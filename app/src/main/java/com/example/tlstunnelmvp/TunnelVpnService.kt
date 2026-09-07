package com.example.tlstunnelmvp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class TunnelVpnService : VpnService() {

    companion object {
        private const val CHANNEL_ID = "tls_tunnel_channel"
        private const val NOTIFICATION_ID = 1001

        const val EXTRA_HOST = "server_host"
        const val EXTRA_PORT = "server_port"

        private const val DEFAULT_HOST = "tlstunnemvp.fly.dev"
        private const val DEFAULT_PORT = 443

        private const val PROTOCOL = "TLSTUNNEL-MVP/2"
    }

    private var vpnInterface: android.os.ParcelFileDescriptor? = null
    private var tlsSocket: SSLSocket? = null

    private val running = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (running.compareAndSet(false, true)) {

            val host =
                intent?.getStringExtra(EXTRA_HOST)
                    ?: DEFAULT_HOST

            val port =
                intent?.getIntExtra(
                    EXTRA_PORT,
                    DEFAULT_PORT
                ) ?: DEFAULT_PORT

            Thread {
                connectionLoop(host, port)
            }.start()
        }

        return START_STICKY
    }

    private fun connectionLoop(
        host: String,
        port: Int
    ) {

        while (running.get()) {

            try {

                establishVpn()

                connectTls(host, port)

                while (
                    running.get() &&
                    tlsSocket != null &&
                    !tlsSocket!!.isClosed
                ) {

                    val socket = tlsSocket!!

                    if (socket.inputStream.read() == -1) {
                        throw Exception("Servidor encerrou a conexão")
                    }
                }

            } catch (e: Exception) {

                e.printStackTrace()

            } finally {

                closeConnection()
            }

            if (running.get()) {
                try {
                    Thread.sleep(3000)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }

        closeConnection()
    }

    private fun establishVpn() {

        if (vpnInterface != null) {
            return
        }

        vpnInterface = Builder()
            .setSession("TLS Tunnel MVP")
            .addAddress("10.8.0.2", 32)
            .establish()
    }

    private fun connectTls(
        host: String,
        port: Int
    ) {

        closeSocketOnly()

        /*
         * IMPORTANTE:
         * Primeiro criamos o socket normal.
         * Depois protegemos contra a VPN.
         * Só então conectamos.
         */

        val rawSocket = Socket()

        if (!protect(rawSocket)) {
            rawSocket.close()
            throw Exception("Não foi possível proteger o socket")
        }

        rawSocket.connect(
            InetSocketAddress(host, port),
            15000
        )

        rawSocket.keepAlive = true
        rawSocket.tcpNoDelay = true

        val factory =
            SSLSocketFactory.getDefault()
                as SSLSocketFactory

        val socket =
            factory.createSocket(
                rawSocket,
                host,
                port,
                true
            ) as SSLSocket

        tlsSocket = socket

        socket.soTimeout = 60000

        socket.startHandshake()

        val output = socket.outputStream

        output.write(
            PROTOCOL.toByteArray(Charsets.UTF_8)
        )

        output.flush()

        /*
         * Se chegamos aqui, o TLS foi estabelecido
         * e o protocolo foi enviado ao servidor.
         */
        println("TLS conectado em $host:$port")
        println("Protocolo enviado: $PROTOCOL")
    }

    private fun closeSocketOnly() {

        try {
            tlsSocket?.close()
        } catch (_: Exception) {
        }

        tlsSocket = null
    }

    private fun closeConnection() {

        closeSocketOnly()

        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        }

        vpnInterface = null
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "TLS Tunnel",
                    NotificationManager.IMPORTANCE_LOW
                )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {

        return Notification.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("TLS Tunnel MVP")
            .setContentText("Conexão TLS ativa")
            .setSmallIcon(
                android.R.drawable.stat_sys_warning
            )
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {

        running.set(false)

        closeConnection()

        super.onDestroy()
    }

    override fun onRevoke() {

        running.set(false)

        closeConnection()

        stopSelf()

        super.onRevoke()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }
}
