package com.example.property_listing_system.background.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.property_listing_system.background.notifications.PropertyNotificationManager
import com.example.property_listing_system.data.network.client.NetworkResult
import com.example.property_listing_system.data.network.repository.PropertyNetworkRepository

class PropertySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME_PERIODIC = "work_periodic_property_sync"
        const val WORK_NAME_ONE_TIME = "work_onetime_property_sync"

        const val KEY_OUTPUT_SYNCED_COUNT = "synced_count"
        const val KEY_OUTPUT_TIMESTAMP = "sync_timestamp"
        const val KEY_OUTPUT_MESSAGE = "sync_message"
    }

    private val repository = PropertyNetworkRepository()
    private val notificationManager = PropertyNotificationManager(appContext)

    override suspend fun doWork(): Result {
        return try {
            val result = repository.fetchRemoteProperties(lastSyncTimestamp = 0)

            when (result) {
                is NetworkResult.Success -> {
                    val propertyList = result.data
                    val syncedCount = propertyList.size

                    // Display notification on device
                    notificationManager.showSyncSuccessNotification(syncedCount)

                    val outputData = workDataOf(
                        KEY_OUTPUT_SYNCED_COUNT to syncedCount,
                        KEY_OUTPUT_TIMESTAMP to System.currentTimeMillis(),
                        KEY_OUTPUT_MESSAGE to "Successfully synchronized $syncedCount properties"
                    )

                    Result.success(outputData)
                }
                is NetworkResult.Error -> {
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure(
                            workDataOf(KEY_OUTPUT_MESSAGE to (result.message))
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf(KEY_OUTPUT_MESSAGE to (e.localizedMessage ?: "Unknown error"))
                )
            }
        }
    }
}
