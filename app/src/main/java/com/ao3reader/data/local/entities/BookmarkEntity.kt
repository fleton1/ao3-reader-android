package com.ao3reader.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
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
data class BookmarkEntity(
    @PrimaryKey
    val workId: String,
    val currentChapter: Int = 1,
    val scrollPosition: Int = 0,
    val progress: Float = 0f, // 0.0 to 1.0
    val bookmarkedAt: Long = System.currentTimeMillis(),
    val lastReadAt: Long = System.currentTimeMillis(),
    val notes: String? = null
)
