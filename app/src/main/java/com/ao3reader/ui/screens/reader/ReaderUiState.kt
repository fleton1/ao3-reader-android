package com.ao3reader.ui.screens.reader

import com.ao3reader.domain.models.Chapter
import com.ao3reader.domain.models.Work

/**
 * UI state for the reader screen.
 */
data class ReaderUiState(
    val work: Work? = null,
    val chapter: Chapter? = null,
    val currentChapter: Int = 1,
    val totalChapters: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val fontSize: Float = 16f,
    val lineHeight: Float = 1.5f
)
