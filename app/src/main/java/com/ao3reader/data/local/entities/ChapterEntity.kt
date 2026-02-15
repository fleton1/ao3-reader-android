package com.ao3reader.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
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
data class ChapterEntity(
    @PrimaryKey
    val id: String, // Format: "workId_chapterNumber"
    val workId: String,
    val chapterNumber: Int,
    val title: String?,
    val summary: String?,
    val notes: String?,
    val endNotes: String?,
    val content: String,
    val wordCount: Int,
    val publishedDate: Long?,
    val cachedAt: Long = System.currentTimeMillis()
)
