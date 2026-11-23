package com.example.property_listing_system.background.notifications

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
import com.example.property_listing_system.MainActivity
import com.example.property_listing_system.R

class PropertyNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_SYNC_ID = "channel_property_sync"
        const val CHANNEL_SYNC_NAME = "Property Sync"
        const val CHANNEL_REMINDERS_ID = "channel_property_reminders"
        const val CHANNEL_REMINDERS_NAME = "Property Reminders & Alerts"

        const val NOTIFICATION_ID_SYNC = 1001
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Sync Channel (Low/Default importance)
            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_ID,
                CHANNEL_SYNC_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Background synchronization notifications for property listings"
                enableLights(true)
            }

            // Reminders Channel (High importance)
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                CHANNEL_REMINDERS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for property viewing appointments, inspections and price alerts"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(syncChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    /**
     * Shows notification when background sync finishes successfully
     */
    fun showSyncSuccessNotification(syncedCount: Int) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SYNC_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Property Listings Updated")
            .setContentText("Successfully synced $syncedCount properties with cloud repository.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Background sync finished. $syncedCount listings updated from remote repository with latest prices and availability.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SYNC, builder.build())
    }

    /**
     * Shows an urgent inspection or viewing reminder notification
     */
    fun showReminderNotification(
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}
