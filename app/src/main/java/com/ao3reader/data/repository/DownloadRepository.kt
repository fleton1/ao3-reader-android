package com.ao3reader.data.repository

import com.ao3reader.data.local.dao.ChapterDao
import com.ao3reader.data.local.dao.DownloadDao
import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.local.entities.DownloadEntity
import com.ao3reader.data.local.entities.DownloadStatus
import com.ao3reader.data.remote.AO3Scraper
import com.ao3reader.domain.models.Download
import com.ao3reader.domain.models.toDomain
import com.ao3reader.workers.DownloadProgress
import com.ao3reader.workers.WorkManagerHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing work downloads.
 */
@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao,
    private val workDao: WorkDao,
    private val chapterDao: ChapterDao,
    private val ao3Scraper: AO3Scraper,
    private val workManagerHelper: WorkManagerHelper
) {
    /**
     * Gets all downloads with their associated works.
     */
    fun getAllDownloads(): Flow<List<Download>> {
        return downloadDao.getAllDownloads().map { downloads ->
            downloads.map { download ->
                val work = workDao.getWorkOnce(download.workId)?.toDomain(isDownloaded = true)
                download.toDomain(work)
            }
        }
    }

    /**
     * Gets completed downloads.
     */
    fun getCompletedDownloads(): Flow<List<Download>> {
        return downloadDao.getCompletedDownloads().map { downloads ->
            downloads.map { download ->
                val work = workDao.getWorkOnce(download.workId)?.toDomain(isDownloaded = true)
                download.toDomain(work)
            }
        }
    }

    /**
     * Gets active (in-progress) downloads.
     */
    fun getActiveDownloads(): Flow<List<Download>> {
        return downloadDao.getActiveDownloads().map { downloads ->
            downloads.map { download ->
                val work = workDao.getWorkOnce(download.workId)?.toDomain()
                download.toDomain(work)
            }
        }
    }

    /**
     * Gets a specific download.
     */
    fun getDownload(workId: String): Flow<Download?> {
        return downloadDao.getDownload(workId).map { download ->
            download?.let {
                val work = workDao.getWorkOnce(workId)?.toDomain(isDownloaded = it.status == DownloadStatus.COMPLETED)
                it.toDomain(work)
            }
        }
    }

    /**
     * Checks if a work is downloaded.
     */
    fun isDownloaded(workId: String): Flow<Boolean> {
        return downloadDao.isDownloadedFlow(workId)
    }

    /**
     * Starts downloading a work using WorkManager for background processing.
     * Returns the work request ID for tracking progress.
     */
    suspend fun startDownload(workId: String, workTitle: String, totalChapters: Int): UUID {
        // Create download entry in database
        val download = DownloadEntity(
            workId = workId,
            status = DownloadStatus.PENDING,
            totalChapters = totalChapters
        )
        downloadDao.insertDownload(download)

        // Schedule background download work
        return workManagerHelper.scheduleDownload(workId, workTitle, totalChapters)
    }

    /**
     * Gets the progress of a download work request.
     */
    fun getDownloadProgress(workRequestId: UUID): Flow<DownloadProgress> {
        return workManagerHelper.getDownloadProgress(workRequestId)
    }

    /**
     * Updates download progress.
     */
    suspend fun updateDownloadProgress(workId: String, downloadedChapters: Int) {
        downloadDao.updateDownloadProgress(
            workId = workId,
            status = DownloadStatus.IN_PROGRESS,
            downloadedChapters = downloadedChapters
        )
    }

    /**
     * Cancels a download.
     */
    suspend fun cancelDownload(workId: String) {
        // Cancel WorkManager work
        workManagerHelper.cancelDownload(workId)

        // Update database status
        downloadDao.getDownloadOnce(workId)?.let { download ->
            downloadDao.updateDownload(
                download.copy(status = DownloadStatus.CANCELLED)
            )
        }
    }

    /**
     * Deletes a download and its chapters.
     */
    suspend fun deleteDownload(workId: String) {
        chapterDao.deleteChaptersForWork(workId)
        downloadDao.deleteDownload(workId)
    }

    /**
     * Clears failed downloads.
     */
    suspend fun clearFailedDownloads() {
        downloadDao.clearFailedDownloads()
    }
}
