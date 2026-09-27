package com.aman.shiii_android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aman.shiii_android.MainActivity
import com.aman.shiii_android.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiiiNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_PRIVATE_ID = "shiii_private_channel"
        const val CHANNEL_GROUP_ID = "shiii_group_channel"
        const val CHANNEL_DIRECT_ID = "shiii_direct_channel"
        private const val NOTIF_PRIVATE_BASE_ID = 2001
        private const val NOTIF_GROUP_BASE_ID = 3001
        private const val NOTIF_DIRECT_BASE_ID = 4001
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val privateChannel = NotificationChannel(
                CHANNEL_PRIVATE_ID,
                "Shiii Private Messages 💕",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when Shiii leaves an advice or private response"
                enableVibration(true)
                setShowBadge(true)
            }

            val groupChannel = NotificationChannel(
                CHANNEL_GROUP_ID,
                "Couple Peace Lounge 🕊️",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when Master, Mistress, or Shiii chat in the 3-Way Group Lounge"
                enableVibration(true)
                setShowBadge(true)
            }

            val directChannel = NotificationChannel(
                CHANNEL_DIRECT_ID,
                "Partner Personal Messages 💌",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time personal messages between Master and Mistress"
                enableVibration(true)
                setShowBadge(true)
            }

            manager.createNotificationChannel(privateChannel)
            manager.createNotificationChannel(groupChannel)
            manager.createNotificationChannel(directChannel)
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showPrivateChatNotification(sender: String, message: String) {
        if (!hasPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "private_chat")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PRIVATE_ID)
            .setSmallIcon(R.drawable.shiii_idle)
            .setContentTitle("Shiii 💕 ($sender)")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_PRIVATE_BASE_ID, notification)
    }

    fun showGroupChatNotification(sender: String, message: String) {
        if (!hasPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "group_chat")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (sender.lowercase()) {
            "shiii", "shiii 🌸" -> "Shiii Peacemaker 🌸🕊️"
            "master" -> "Master 🎩 (Couple Lounge)"
            "mistress" -> "Mistress 💕 (Couple Lounge)"
            else -> "$sender (Couple Lounge 🕊️)"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_GROUP_ID)
            .setSmallIcon(R.drawable.shiii_idle)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_GROUP_BASE_ID, notification)
    }

    fun showDirectChatNotification(sender: String, message: String) {
        if (!hasPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "direct_chat")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DIRECT_ID)
            .setSmallIcon(R.drawable.shiii_idle)
            .setContentTitle("$sender 💌")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_DIRECT_BASE_ID, notification)
    }
}
