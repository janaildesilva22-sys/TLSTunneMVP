package com.example.tlstunnelmvp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class TunnelVpnService : VpnService() {

    companion object {
        private const val CHANNEL_ID = "tls_tunnel_channel"
        private const val NOTIFICATION_ID = 1001

        const val EXTRA_HOST = "server_host"
        const val EXTRA_PORT = "server_port"

        const val ACTION_STATUS =
            "com.example.tlstunnelmvp.TUNNEL_STATUS"

        const val EXTRA_CONNECTED =
            "connected"

        private const val DEFAULT_HOST =
            "tlstunnemvp.fly.dev"

        private const val DEFAULT_PORT = 443

        private const val PROTOCOL =
            "TLSTUNNEL-MVP/2"
    }

    private var vpnInterface:
        android.os.ParcelFileDescriptor? = null

    private var tlsSocket:
        SSLSocket? = null

    @Volatile
    private var running = false

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

        if (running) {
            return START_STICKY
        }

        val host =
            intent?.getStringExtra(EXTRA_HOST)
                ?: DEFAULT_HOST

        val port =
            intent?.getIntExtra(
                EXTRA_PORT,
                DEFAULT_PORT
            ) ?: DEFAULT_PORT

        running = true

        sendStatus(false)

        Thread {
            runTunnel(host, port)
        }.start()

        return START_STICKY
    }

    private fun runTunnel(
        host: String,
        port: Int
    ) {

        try {

            vpnInterface = Builder()
                .setSession("TLS Tunnel MVP")
                .addAddress(
                    "10.8.0.2",
                    32
                )
                .establish()

            if (vpnInterface == null) {
                sendStatus(false)
                stopSelf()
                return
            }

            val socketFactory =
                SSLSocketFactory.getDefault()
                    as SSLSocketFactory

            val socket =
                socketFactory.createSocket(
                    host,
                    port
                ) as SSLSocket

            tlsSocket = socket

            if (!protect(socket)) {
                socket.close()
                sendStatus(false)
                stopSelf()
                return
            }

            socket.startHandshake()

            val output =
                BufferedOutputStream(
                    socket.outputStream
                )

            val input =
                BufferedInputStream(
                    socket.inputStream
                )

            output.write(
                PROTOCOL.toByteArray()
            )

            output.flush()

            // Só consideramos conectado depois
            // que o TLS foi estabelecido e o
            // protocolo inicial foi enviado.
            sendStatus(true)

            val buffer =
                ByteArray(1024)

            while (
                running &&
                !socket.isClosed
            ) {

                val count =
                    input.read(buffer)

                if (count < 0) {
                    break
                }
            }

        } catch (error: Exception) {

            error.printStackTrace()

            sendStatus(false)

        } finally {

            try {
                tlsSocket?.close()
            } catch (_: Exception) {
            }

            tlsSocket = null

            try {
                vpnInterface?.close()
            } catch (_: Exception) {
            }

            vpnInterface = null

            running = false

            sendStatus(false)
        }
    }

    private fun sendStatus(
        connected: Boolean
    ) {

        val intent =
            Intent(ACTION_STATUS)

        intent.setPackage(packageName)

        intent.putExtra(
            EXTRA_CONNECTED,
            connected
        )

        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

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

            manager.createNotificationChannel(
                channel
            )
        }
    }

    private fun createNotification():
        Notification {

        return Notification.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle(
                "TLS Tunnel MVP"
            )
            .setContentText(
                "Túnel TLS em execução"
            )
            .setSmallIcon(
                android.R.drawable.stat_sys_warning
            )
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {

        running = false

        try {
            tlsSocket?.close()
        } catch (_: Exception) {
        }

        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        }

        tlsSocket = null
        vpnInterface = null

        sendStatus(false)

        super.onDestroy()
    }

    override fun onRevoke() {

        running = false

        try {
            tlsSocket?.close()
        } catch (_: Exception) {
        }

        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        }

        sendStatus(false)

        stopSelf()

        super.onRevoke()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return super.onBind(intent)
    }
}
