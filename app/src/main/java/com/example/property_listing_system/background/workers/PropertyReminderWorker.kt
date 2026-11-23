package com.example.property_listing_system.background.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.property_listing_system.background.notifications.PropertyNotificationManager

class PropertyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_INPUT_PROPERTY_ID = "key_property_id"
        const val KEY_INPUT_PROPERTY_TITLE = "key_property_title"
        const val KEY_INPUT_REMINDER_MESSAGE = "key_reminder_message"
        const val KEY_INPUT_REMINDER_TYPE = "key_reminder_type"

        const val TAG_REMINDER = "tag_property_reminder"
    }

    private val notificationManager = PropertyNotificationManager(appContext)

    override suspend fun doWork(): Result {
        val propertyId = inputData.getString(KEY_INPUT_PROPERTY_ID) ?: "PROP_GENERAL"
        val propertyTitle = inputData.getString(KEY_INPUT_PROPERTY_TITLE) ?: "Property Listing"
        val reminderMessage = inputData.getString(KEY_INPUT_REMINDER_MESSAGE)
            ?: "Reminder: Scheduled viewing or inspection for $propertyTitle"
        val reminderType = inputData.getString(KEY_INPUT_REMINDER_TYPE) ?: "Appointment"

        val title = "Property Alert: $reminderType"
        val fullMessage = "$propertyTitle: $reminderMessage"

        notificationManager.showReminderNotification(
            title = title,
            message = fullMessage,
            notificationId = propertyId.hashCode()
        )

        val output = workDataOf(
            "status" to "Triggered",
            "property_id" to propertyId
        )

        return Result.success(output)
    }
}
