package com.ao3reader.domain.models

import com.ao3reader.data.local.entities.WorkEntity

/**
 * Domain model for an AO3 work.
 * Clean business object used throughout the app.
 */
data class Work(
    val id: String,
    val title: String,
    val author: String,
    val authorId: String?,
    val summary: String,
    val rating: Rating,
    val warnings: List<String>,
    val categories: List<String>,
    val fandoms: List<String>,
    val relationships: List<String>,
    val characters: List<String>,
    val additionalTags: List<String>,
    val language: String,
    val publishedDate: Long,
    val updatedDate: Long,
    val words: Int,
    val currentChapters: Int,
    val totalChapters: String,
    val kudos: Int,
    val bookmarksCount: Int,
    val hits: Int,
    val isComplete: Boolean,
    val isSeries: Boolean = false,
    val seriesName: String? = null,
    val seriesPart: Int? = null,
    val isBookmarked: Boolean = false,
    val isDownloaded: Boolean = false,
    val isFollowing: Boolean = false
)

/**
 * Extension function to convert WorkEntity to domain Work model.
 */
fun WorkEntity.toDomain(
    isBookmarked: Boolean = false,
    isDownloaded: Boolean = false,
    isFollowing: Boolean = false
): Work {
    return Work(
        id = id,
        title = title,
        author = author,
        authorId = authorId,
        summary = summary,
        rating = Rating.fromString(rating),
        warnings = warnings.split(", ").filter { it.isNotBlank() },
        categories = category.split(", ").filter { it.isNotBlank() },
        fandoms = fandom.split(", ").filter { it.isNotBlank() },
        relationships = relationships.split(", ").filter { it.isNotBlank() },
        characters = characters.split(", ").filter { it.isNotBlank() },
        additionalTags = additionalTags.split(", ").filter { it.isNotBlank() },
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
        isBookmarked = isBookmarked,
        isDownloaded = isDownloaded,
        isFollowing = isFollowing
    )
}
