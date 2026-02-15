package com.ao3reader.ui.screens.workdetail

import com.ao3reader.domain.models.Work

/**
 * UI state for the work detail screen.
 */
data class WorkDetailUiState(
    val work: Work? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isBookmarked: Boolean = false,
    val isDownloaded: Boolean = false,
    val isFollowing: Boolean = false,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false
)
