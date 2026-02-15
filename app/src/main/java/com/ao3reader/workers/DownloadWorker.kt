package com.ao3reader.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ao3reader.R
import com.ao3reader.data.local.dao.ChapterDao
import com.ao3reader.data.local.dao.DownloadDao
import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.local.entities.DownloadStatus
import com.ao3reader.data.remote.AO3Scraper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

/**
 * WorkManager worker for downloading works in the background.
 * Supports pause, resume, and progress notifications.
 */
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val ao3Scraper: AO3Scraper,
    private val workDao: WorkDao,
    private val chapterDao: ChapterDao,
    private val downloadDao: DownloadDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_WORK_ID = "work_id"
        const val KEY_WORK_TITLE = "work_title"
        const val KEY_TOTAL_CHAPTERS = "total_chapters"
        const val KEY_PROGRESS = "progress"
        const val KEY_DOWNLOADED_CHAPTERS = "downloaded_chapters"

        private const val NOTIFICATION_CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Work Downloads"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        val workId = inputData.getString(KEY_WORK_ID) ?: return Result.failure()
        val workTitle = inputData.getString(KEY_WORK_TITLE) ?: "Unknown Work"
        val totalChapters = inputData.getInt(KEY_TOTAL_CHAPTERS, 1)

        // Create notification channel
        createNotificationChannel()

        // Set foreground service
        setForeground(createForegroundInfo(workTitle, 0, totalChapters))

        return try {
            // Update download status to IN_PROGRESS
            downloadDao.updateDownloadProgress(
                workId = workId,
                status = DownloadStatus.IN_PROGRESS,
                downloadedChapters = 0
            )

            // Download all chapters
            ao3Scraper.getAllChapters(workId)
                .onSuccess { chapterDtos ->
                    // Save chapters to database with progress updates
                    chapterDtos.forEachIndexed { index, chapterDto ->
                        val entity = chapterDto.toEntity()
                        chapterDao.insertChapter(entity)

                        val downloadedCount = index + 1
                        val progress = downloadedCount.toFloat() / totalChapters.toFloat()

                        // Update progress in database
                        downloadDao.updateDownloadProgress(
                            workId = workId,
                            status = DownloadStatus.IN_PROGRESS,
                            downloadedChapters = downloadedCount
                        )

                        // Update notification
                        setForeground(createForegroundInfo(workTitle, downloadedCount, totalChapters))

                        // Update work data for progress tracking
                        setProgressAsync(workDataOf(
                            KEY_PROGRESS to progress,
                            KEY_DOWNLOADED_CHAPTERS to downloadedCount
                        ))

                        // Small delay to allow UI to update
                        delay(100)
                    }

                    // Mark download as completed
                    downloadDao.completeDownload(
                        workId = workId,
                        status = DownloadStatus.COMPLETED
                    )

                    // Show completion notification
                    showCompletionNotification(workTitle)

                    Result.success(workDataOf(
                        KEY_PROGRESS to 1f,
                        KEY_DOWNLOADED_CHAPTERS to totalChapters
                    ))
                }
                .onFailure { error ->
                    // Mark download as failed
                    downloadDao.failDownload(
                        workId = workId,
                        errorMessage = error.message ?: "Download failed"
                    )

                    Result.failure(workDataOf(
                        "error" to (error.message ?: "Download failed")
                    ))
                }
                .getOrThrow()

        } catch (e: Exception) {
            // Handle unexpected errors
            downloadDao.failDownload(
                workId = workId,
                errorMessage = e.message ?: "Unexpected error"
            )

            Result.failure(workDataOf(
                "error" to (e.message ?: "Unexpected error")
            ))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of work downloads"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundInfo(
        workTitle: String,
        downloadedChapters: Int,
        totalChapters: Int
    ): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Downloading: $workTitle")
            .setContentText("$downloadedChapters of $totalChapters chapters")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(totalChapters, downloadedChapters, false)
            .setOngoing(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(workTitle: String) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText(workTitle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
}
