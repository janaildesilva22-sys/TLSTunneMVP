package com.example.tlstunnelmvp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder

class TunnelVpnService : VpnService() {

    companion object {
        private const val CHANNEL_ID = "tls_tunnel_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val notification = createNotification()

        startForeground(
            NOTIFICATION_ID,
            notification
        )

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "TLS Tunnel",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("TLS Tunnel MVP")
            .setContentText("Serviço de túnel ativo")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }
}
