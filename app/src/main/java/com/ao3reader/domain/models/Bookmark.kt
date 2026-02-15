package com.ao3reader.domain.models

import com.ao3reader.data.local.entities.BookmarkEntity

/**
 * Domain model for a bookmarked work with reading progress.
 */
data class Bookmark(
    val workId: String,
    val work: Work?,
    val currentChapter: Int,
    val scrollPosition: Int,
    val progress: Float,
    val bookmarkedAt: Long,
    val lastReadAt: Long,
    val notes: String?
)

/**
 * Extension function to convert BookmarkEntity to domain Bookmark model.
 */
fun BookmarkEntity.toDomain(work: Work? = null): Bookmark {
    return Bookmark(
        workId = workId,
        work = work,
        currentChapter = currentChapter,
        scrollPosition = scrollPosition,
        progress = progress,
        bookmarkedAt = bookmarkedAt,
        lastReadAt = lastReadAt,
        notes = notes
    )
}
