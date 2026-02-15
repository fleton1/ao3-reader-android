package com.ao3reader.data.remote.models

import com.ao3reader.data.local.entities.WorkEntity

/**
 * Data Transfer Object for a work fetched from AO3.
 * Maps to WorkEntity for local storage.
 */
data class WorkDto(
    val id: String,
    val title: String,
    val author: String,
    val authorId: String? = null,
    val summary: String,
    val rating: String,
    val warnings: String,
    val category: String,
    val fandom: String,
    val relationships: String,
    val characters: String,
    val additionalTags: String,
    val language: String,
    val publishedDate: Long,
    val updatedDate: Long,
    val words: Int,
    val currentChapters: Int,
    val totalChapters: String, // Can be "?" for ongoing works
    val kudos: Int,
    val bookmarksCount: Int,
    val hits: Int,
    val isComplete: Boolean,
    val isSeries: Boolean = false,
    val seriesName: String? = null,
    val seriesPart: Int? = null
) {
    /**
     * Converts this DTO to a WorkEntity for database storage.
     */
    fun toEntity(): WorkEntity {
        return WorkEntity(
            id = id,
            title = title,
            author = author,
            authorId = authorId,
            summary = summary,
            rating = rating,
            warnings = warnings,
            category = category,
            fandom = fandom,
            relationships = relationships,
            characters = characters,
            additionalTags = additionalTags,
            language = language,
            publishedDate = publishedDate,
            updatedDate = updatedDate,
            words = words,
            currentChapters = currentChapters,
            totalChapters = totalChapters,
            kudos = kudos,
            bookmarksCount = bookmarksCount,
            hits = hits,
            isComplete = isComplete,
            isSeries = isSeries,
            seriesName = seriesName,
            seriesPart = seriesPart,
            cachedAt = System.currentTimeMillis()
        )
    }
}
