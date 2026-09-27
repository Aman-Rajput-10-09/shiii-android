package com.aman.shiii_android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aman.shiii_android.MainActivity
import com.aman.shiii_android.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiiiNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ShiiiNotifHelper"
        const val CHANNEL_PRIVATE_ID = "shiii_private_channel_v3"
        const val CHANNEL_GROUP_ID = "shiii_group_channel_v3"
        const val CHANNEL_DIRECT_ID = "shiii_direct_channel_v3"
        private val notifCounter = AtomicInteger(2000)
    }

    private var cachedAvatarBitmap: Bitmap? = null

    init {
        createNotificationChannels()
    }

    private fun getAvatarBitmap(): Bitmap? {
        if (cachedAvatarBitmap == null) {
            try {
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = 4 // Downsample 836x935 to ~209x233
                }
                val raw = BitmapFactory.decodeResource(context.resources, R.drawable.shiii_idle, opts)
                if (raw != null) {
                    cachedAvatarBitmap = Bitmap.createScaledBitmap(raw, 128, 128, true)
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to decode Shiii avatar for notification: ${e.message}")
            }
        }
        return cachedAvatarBitmap
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val privateChannel = NotificationChannel(
                CHANNEL_PRIVATE_ID,
                "Shiii Private Messages 💕",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when Shiii leaves an advice or private response"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 180, 80, 180)
                setSound(defaultSoundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val groupChannel = NotificationChannel(
                CHANNEL_GROUP_ID,
                "Couple Peace Lounge 🕊️",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when Master, Mistress, or Shiii chat in the 3-Way Group Lounge"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                setSound(defaultSoundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val directChannel = NotificationChannel(
                CHANNEL_DIRECT_ID,
                "Partner Personal Messages 💌",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time personal messages between Master and Mistress"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
                setSound(defaultSoundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            manager.createNotificationChannel(privateChannel)
            manager.createNotificationChannel(groupChannel)
            manager.createNotificationChannel(directChannel)
            Log.i(TAG, "Notification channels v3 initialized successfully")
        }
    }

    fun hasPermission(): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are globally disabled for this app in system settings")
            return false
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w(TAG, "POST_NOTIFICATIONS runtime permission has not been granted yet")
            }
            granted
        } else {
            true
        }
    }

    fun showPrivateChatNotification(sender: String, message: String, msgId: Int? = null) {
        if (!hasPermission()) {
            Log.w(TAG, "Cannot show private chat notification: permission missing")
            return
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "private_chat")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_PRIVATE_ID)
                .setSmallIcon(R.drawable.ic_stat_shiii)
                .setColor(0xFFEA5E8C.toInt())
                .setContentTitle("Shiii 💕 ($sender)")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            try {
                getAvatarBitmap()?.let { builder.setLargeIcon(it) }
            } catch (t: Throwable) {
                Log.w(TAG, "Could not set large icon: ${t.message}")
            }

            val notifId = msgId ?: notifCounter.incrementAndGet()
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
            Log.i(TAG, "Private chat notification posted successfully (ID: $notifId)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post private chat notification", e)
        }
    }

    fun showGroupChatNotification(sender: String, message: String, msgId: Int? = null) {
        if (!hasPermission()) {
            Log.w(TAG, "Cannot show group chat notification: permission missing")
            return
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "group_chat")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                102,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = when (sender.lowercase()) {
                "shiii", "shiii 🌸" -> "Shiii Peacemaker 🌸🕊️"
                "master" -> "Master 🎩 (Couple Lounge)"
                "mistress" -> "Mistress 💕 (Couple Lounge)"
                else -> "$sender (Couple Lounge 🕊️)"
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_GROUP_ID)
                .setSmallIcon(R.drawable.ic_stat_shiii)
                .setColor(0xFFEA5E8C.toInt())
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            try {
                getAvatarBitmap()?.let { builder.setLargeIcon(it) }
            } catch (t: Throwable) {
                Log.w(TAG, "Could not set large icon: ${t.message}")
            }

            val notifId = msgId ?: notifCounter.incrementAndGet()
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
            Log.i(TAG, "Group chat notification posted successfully (ID: $notifId)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post group chat notification", e)
        }
    }

    fun showDirectChatNotification(sender: String, message: String, msgId: Int? = null) {
        if (!hasPermission()) {
            Log.w(TAG, "Cannot show direct chat notification: permission missing")
            return
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "direct_chat")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                103,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_DIRECT_ID)
                .setSmallIcon(R.drawable.ic_stat_shiii)
                .setColor(0xFFEA5E8C.toInt())
                .setContentTitle("$sender 💌")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            try {
                getAvatarBitmap()?.let { builder.setLargeIcon(it) }
            } catch (t: Throwable) {
                Log.w(TAG, "Could not set large icon: ${t.message}")
            }

            val notifId = msgId ?: notifCounter.incrementAndGet()
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
            Log.i(TAG, "Direct chat notification posted successfully (ID: $notifId)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post direct chat notification", e)
        }
    }
}

