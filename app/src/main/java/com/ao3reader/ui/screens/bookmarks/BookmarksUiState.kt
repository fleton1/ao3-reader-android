package com.ao3reader.ui.screens.bookmarks

import com.ao3reader.domain.models.Bookmark

/**
 * UI state for the bookmarks screen.
 */
data class BookmarksUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
