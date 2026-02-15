package com.ao3reader.ui.screens.downloads

import com.ao3reader.domain.models.Download

/**
 * UI state for the downloads screen.
 */
data class DownloadsUiState(
    val downloads: List<Download> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
