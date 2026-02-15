package com.ao3reader.ui.screens.following

import com.ao3reader.domain.models.Following

/**
 * UI state for the following screen.
 */
data class FollowingUiState(
    val following: List<Following> = emptyList(),
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val error: String? = null
)
