package com.ao3reader.domain.models

import com.ao3reader.data.local.entities.DownloadEntity
import com.ao3reader.data.local.entities.DownloadStatus

/**
 * Domain model for a work download with progress tracking.
 */
data class Download(
    val workId: String,
    val work: Work?,
    val status: DownloadStatus,
    val totalChapters: Int,
    val downloadedChapters: Int,
    val progress: Float,
    val startedAt: Long,
    val completedAt: Long?,
    val errorMessage: String?
) {
    val isComplete: Boolean
        get() = status == DownloadStatus.COMPLETED

    val isInProgress: Boolean
        get() = status == DownloadStatus.IN_PROGRESS

    val hasFailed: Boolean
        get() = status == DownloadStatus.FAILED
}

/**
 * Extension function to convert DownloadEntity to domain Download model.
 */
fun DownloadEntity.toDomain(work: Work? = null): Download {
    val progress = if (totalChapters > 0) {
        downloadedChapters.toFloat() / totalChapters.toFloat()
    } else {
        0f
    }

    return Download(
        workId = workId,
        work = work,
        status = status,
        totalChapters = totalChapters,
        downloadedChapters = downloadedChapters,
        progress = progress,
        startedAt = startedAt,
        completedAt = completedAt,
        errorMessage = errorMessage
    )
}
