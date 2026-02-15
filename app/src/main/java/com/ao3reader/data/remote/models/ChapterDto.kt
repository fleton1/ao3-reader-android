package com.ao3reader.data.remote.models

import com.ao3reader.data.local.entities.ChapterEntity

/**
 * Data Transfer Object for a chapter fetched from AO3.
 * Maps to ChapterEntity for local storage.
 */
data class ChapterDto(
    val workId: String,
    val chapterNumber: Int,
    val title: String?,
    val summary: String?,
    val notes: String?,
    val endNotes: String?,
    val content: String,
    val wordCount: Int,
    val publishedDate: Long? = null
) {
    /**
     * Converts this DTO to a ChapterEntity for database storage.
     */
    fun toEntity(): ChapterEntity {
        return ChapterEntity(
            id = "${workId}_$chapterNumber",
            workId = workId,
            chapterNumber = chapterNumber,
            title = title,
            summary = summary,
            notes = notes,
            endNotes = endNotes,
            content = content,
            wordCount = wordCount,
            publishedDate = publishedDate,
            cachedAt = System.currentTimeMillis()
        )
    }
}
