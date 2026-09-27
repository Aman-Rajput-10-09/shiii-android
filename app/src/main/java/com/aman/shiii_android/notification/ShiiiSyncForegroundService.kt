package com.aman.shiii_android.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aman.shiii_android.MainActivity
import com.aman.shiii_android.R
import com.aman.shiii_android.data.local.AuthPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShiiiSyncForegroundService : Service() {

    companion object {
        private const val TAG = "ShiiiSyncService"
        const val CHANNEL_SERVICE_ID = "shiii_sync_service_channel_v1"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            try {
                val intent = Intent(context, ShiiiSyncForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.i(TAG, "Requested start of ShiiiSyncForegroundService")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start ShiiiSyncForegroundService", e)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, ShiiiSyncForegroundService::class.java)
                context.stopService(intent)
                Log.i(TAG, "Requested stop of ShiiiSyncForegroundService")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop ShiiiSyncForegroundService", e)
            }
        }
    }

    @Inject
    lateinit var authPreferences: AuthPreferences

    @Inject
    lateinit var syncManager: RealtimeNotificationSyncManager

    override fun onCreate() {
        super.onCreate()
        createServiceChannel()
        startInForeground()
        Log.i(TAG, "ShiiiSyncForegroundService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val user = authPreferences.getUser()
        if (user != null) {
            Log.i(TAG, "Starting sync in foreground service for ${user.username}")
            syncManager.startSync(user)
        } else {
            Log.w(TAG, "No logged in user found, stopping service")
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        syncManager.stopSync()
        Log.i(TAG, "ShiiiSyncForegroundService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_SERVICE_ID,
                "Shiii Background Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Shiii connected with your partner in real time"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun startInForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_SERVICE_ID)
            .setSmallIcon(R.drawable.ic_stat_shiii)
            .setColor(0xFFEA5E8C.toInt())
            .setContentTitle("Shiii Couple Emissary 💕")
            .setContentText("Connected and listening for your partner's messages")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
}
