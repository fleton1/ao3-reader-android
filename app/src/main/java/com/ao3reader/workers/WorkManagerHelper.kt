package com.ao3reader.workers

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing WorkManager tasks.
 */
@Singleton
class WorkManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    /**
     * Schedules a download work request.
     * Returns the work request ID for tracking progress.
     */
    fun scheduleDownload(
        workId: String,
        workTitle: String,
        totalChapters: Int
    ): UUID {
        val inputData = Data.Builder()
            .putString(DownloadWorker.KEY_WORK_ID, workId)
            .putString(DownloadWorker.KEY_WORK_TITLE, workTitle)
            .putInt(DownloadWorker.KEY_TOTAL_CHAPTERS, totalChapters)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        // Use unique work name to prevent duplicate downloads
        workManager.enqueueUniqueWork(
            "download_$workId",
            ExistingWorkPolicy.KEEP,
            downloadRequest
        )

        return downloadRequest.id
    }

    /**
     * Gets the progress of a download work request.
     */
    fun getDownloadProgress(workRequestId: UUID): Flow<DownloadProgress> {
        return workManager.getWorkInfoByIdFlow(workRequestId).map { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> {
                    val progress = workInfo.progress.getFloat(DownloadWorker.KEY_PROGRESS, 0f)
                    val downloadedChapters = workInfo.progress.getInt(
                        DownloadWorker.KEY_DOWNLOADED_CHAPTERS,
                        0
                    )
                    DownloadProgress.InProgress(progress, downloadedChapters)
                }
                WorkInfo.State.SUCCEEDED -> {
                    DownloadProgress.Completed
                }
                WorkInfo.State.FAILED -> {
                    val error = workInfo.outputData.getString("error") ?: "Download failed"
                    DownloadProgress.Failed(error)
                }
                WorkInfo.State.CANCELLED -> {
                    DownloadProgress.Cancelled
                }
                else -> {
                    DownloadProgress.Pending
                }
            }
        }
    }

    /**
     * Cancels a download work request.
     */
    fun cancelDownload(workId: String) {
        workManager.cancelUniqueWork("download_$workId")
    }

    /**
     * Schedules periodic update checking for followed works.
     * Runs every 6 hours.
     */
    fun schedulePeriodicUpdateCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateCheckRequest = PeriodicWorkRequestBuilder<UpdateCheckerWorker>(
            repeatInterval = 6,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            UpdateCheckerWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            updateCheckRequest
        )
    }

    /**
     * Triggers an immediate update check.
     */
    fun triggerUpdateCheck(): UUID {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateCheckRequest = OneTimeWorkRequestBuilder<UpdateCheckerWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(updateCheckRequest)

        return updateCheckRequest.id
    }

    /**
     * Cancels periodic update checking.
     */
    fun cancelPeriodicUpdateCheck() {
        workManager.cancelUniqueWork(UpdateCheckerWorker.WORK_NAME)
    }
}

/**
 * Sealed class representing download progress states.
 */
sealed class DownloadProgress {
    data object Pending : DownloadProgress()
    data class InProgress(val progress: Float, val downloadedChapters: Int) : DownloadProgress()
    data object Completed : DownloadProgress()
    data class Failed(val error: String) : DownloadProgress()
    data object Cancelled : DownloadProgress()
}
