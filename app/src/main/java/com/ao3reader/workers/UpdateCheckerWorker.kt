package com.ao3reader.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ao3reader.R
import com.ao3reader.data.repository.FollowingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker for checking updates to followed works.
 * Runs periodically in the background and sends notifications for new chapters.
 */
@HiltWorker
class UpdateCheckerWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val followingRepository: FollowingRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "update_checker"
        private const val NOTIFICATION_CHANNEL_ID = "updates_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Work Updates"
        private const val NOTIFICATION_ID = 2001
    }

    override suspend fun doWork(): Result {
        // Create notification channel
        createNotificationChannel()

        return try {
            // Check for updates
            val result = followingRepository.checkForUpdates()

            result.fold(
                onSuccess = { updateCount ->
                    if (updateCount > 0) {
                        // Show notification about updates
                        showUpdateNotification(updateCount)
                    }

                    Result.success()
                },
                onFailure = { error ->
                    // Retry on failure
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )

        } catch (e: Exception) {
            // Retry on unexpected errors
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for updates to followed works"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showUpdateNotification(updateCount: Int) {
        val title = if (updateCount == 1) {
            "1 work has new chapters"
        } else {
            "$updateCount works have new chapters"
        }

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Tap to view updates")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
