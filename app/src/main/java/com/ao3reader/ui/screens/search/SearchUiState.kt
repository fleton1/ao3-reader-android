package com.ao3reader.ui.screens.search

import com.ao3reader.domain.models.SearchFilters
import com.ao3reader.domain.models.Work

/**
 * UI state for the search screen.
 */
data class SearchUiState(
    val query: String = "",
    val filters: SearchFilters = SearchFilters(),
    val results: List<Work> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false
)
