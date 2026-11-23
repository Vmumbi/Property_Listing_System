package com.example.property_listing_system.background.scheduler

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.property_listing_system.background.workers.PropertyReminderWorker
import com.example.property_listing_system.background.workers.PropertySyncWorker
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit

class PropertyWorkManagerScheduler(private val context: Context) {

    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }

    /**
     * Schedules periodic background sync every N hours (default 24 hours).
     * Requires network connectivity and battery not low.
     */
    fun schedulePeriodicSync(repeatIntervalHours: Long = 24) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<PropertySyncWorker>(
            repeatIntervalHours, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // 15-minute flex interval
        )
            .setConstraints(constraints)
            .addTag(PropertySyncWorker.WORK_NAME_PERIODIC)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PropertySyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }

    /**
     * Enqueues an immediate one-time background sync.
     */
    fun scheduleImmediateSync(): UUID {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateSyncRequest = OneTimeWorkRequestBuilder<PropertySyncWorker>()
            .setConstraints(constraints)
            .addTag(PropertySyncWorker.WORK_NAME_ONE_TIME)
            .build()

        workManager.enqueueUniqueWork(
            PropertySyncWorker.WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            immediateSyncRequest
        )

        return immediateSyncRequest.id
    }

    /**
     * Schedules a reminder for property viewing, inspection, or follow-up after a delay.
     * @param delaySeconds time delay before triggering notification (e.g. 10s for testing, or 3600s)
     */
    fun schedulePropertyReminder(
        propertyId: String,
        propertyTitle: String,
        message: String,
        delaySeconds: Long = 10,
        reminderType: String = "Viewing Appointment"
    ): UUID {
        val inputData = workDataOf(
            PropertyReminderWorker.KEY_INPUT_PROPERTY_ID to propertyId,
            PropertyReminderWorker.KEY_INPUT_PROPERTY_TITLE to propertyTitle,
            PropertyReminderWorker.KEY_INPUT_REMINDER_MESSAGE to message,
            PropertyReminderWorker.KEY_INPUT_REMINDER_TYPE to reminderType
        )

        val reminderRequest = OneTimeWorkRequestBuilder<PropertyReminderWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setInputData(inputData)
            .addTag(PropertyReminderWorker.TAG_REMINDER)
            .build()

        workManager.enqueue(reminderRequest)
        return reminderRequest.id
    }

    /**
     * Cancels the periodic sync job.
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(PropertySyncWorker.WORK_NAME_PERIODIC)
    }

    /**
     * Cancels all scheduled property reminders.
     */
    fun cancelAllReminders() {
        workManager.cancelAllWorkByTag(PropertyReminderWorker.TAG_REMINDER)
    }

    /**
     * Returns LiveData to observe sync worker state in UI (ENQUEUED, RUNNING, SUCCEEDED, FAILED).
     */
    fun getSyncWorkInfoLiveData(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(PropertySyncWorker.WORK_NAME_ONE_TIME)
    }

    /**
     * Returns Flow to observe sync worker state in Kotlin coroutines.
     */
    fun getSyncWorkInfoFlow(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow(PropertySyncWorker.WORK_NAME_ONE_TIME)
    }
}
