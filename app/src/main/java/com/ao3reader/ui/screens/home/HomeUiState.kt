package com.ao3reader.ui.screens.home

/**
 * UI state for the home screen.
 */
data class HomeUiState(
    val recentBookmarksCount: Int = 0,
    val downloadsCount: Int = 0,
    val followingCount: Int = 0,
    val updateCount: Int = 0,
    val isLoading: Boolean = false
)
