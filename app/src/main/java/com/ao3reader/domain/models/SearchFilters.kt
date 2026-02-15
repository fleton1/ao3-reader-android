package com.ao3reader.domain.models

/**
 * Search filters for querying AO3 works.
 */
data class SearchFilters(
    val query: String = "",
    val rating: Rating? = null,
    val fandoms: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val sortBy: SortBy = SortBy.UPDATED_DATE,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val isComplete: Boolean? = null,
    val minWords: Int? = null,
    val maxWords: Int? = null
)

enum class SortBy {
    UPDATED_DATE,
    PUBLISHED_DATE,
    WORD_COUNT,
    KUDOS,
    HITS,
    BOOKMARKS,
    TITLE,
    AUTHOR
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}
