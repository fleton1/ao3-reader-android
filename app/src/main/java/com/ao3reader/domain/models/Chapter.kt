package com.ao3reader.domain.models

import com.ao3reader.data.local.entities.ChapterEntity

/**
 * Domain model for a chapter.
 */
data class Chapter(
    val id: String,
    val workId: String,
    val chapterNumber: Int,
    val title: String?,
    val summary: String?,
    val notes: String?,
    val endNotes: String?,
    val content: String,
    val wordCount: Int,
    val publishedDate: Long?
)

/**
 * Extension function to convert ChapterEntity to domain Chapter model.
 */
fun ChapterEntity.toDomain(): Chapter {
    return Chapter(
        id = id,
        workId = workId,
        chapterNumber = chapterNumber,
        title = title,
        summary = summary,
        notes = notes,
        endNotes = endNotes,
        content = content,
        wordCount = wordCount,
        publishedDate = publishedDate
    )
}
