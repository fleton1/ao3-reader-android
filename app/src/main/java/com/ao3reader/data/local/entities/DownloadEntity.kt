package com.ao3reader.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "downloads",
    foreignKeys = [
        ForeignKey(
            entity = WorkEntity::class,
            parentColumns = ["id"],
            childColumns = ["workId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workId")]
)
data class DownloadEntity(
    @PrimaryKey
    val workId: String,
    val status: DownloadStatus,
    val totalChapters: Int,
    val downloadedChapters: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
)

enum class DownloadStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}
