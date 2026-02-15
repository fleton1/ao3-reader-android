package com.ao3reader.ui.screens.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ao3reader.data.repository.BookmarkRepository
import com.ao3reader.data.repository.WorkRepository
import com.ao3reader.domain.models.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val workRepository: WorkRepository,
    private val bookmarkRepository: BookmarkRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workId: String = checkNotNull(savedStateHandle["workId"])
    private val initialChapter: Int = checkNotNull(savedStateHandle["chapterNumber"])

    private val _uiState = MutableStateFlow(ReaderUiState(isLoading = true, currentChapter = initialChapter))
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        loadWork()
        loadChapter(initialChapter)
    }

    private fun loadWork() {
        viewModelScope.launch {
            workRepository.getWork(workId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                work = resource.data,
                                totalChapters = resource.data?.currentChapters ?: 1
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = resource.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Keep loading state
                    }
                }
            }
        }
    }

    fun loadChapter(chapterNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentChapter = chapterNumber) }

            workRepository.getChapter(workId, chapterNumber).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                chapter = resource.data,
                                isLoading = false,
                                error = null
                            )
                        }
                        // Update reading progress in bookmark
                        updateReadingProgress(chapterNumber)
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Failed to load chapter"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateReadingProgress(chapterNumber: Int) {
        viewModelScope.launch {
            // Check if work is bookmarked
            if (bookmarkRepository.isBookmarked(workId)) {
                // Calculate progress
                val totalChapters = _uiState.value.totalChapters
                val progress = if (totalChapters > 0) {
                    chapterNumber.toFloat() / totalChapters.toFloat()
                } else {
                    0f
                }

                bookmarkRepository.updateReadingProgress(
                    workId = workId,
                    currentChapter = chapterNumber,
                    scrollPosition = 0,
                    progress = progress
                )
            } else {
                // Auto-bookmark when starting to read
                bookmarkRepository.addBookmark(workId)
            }
        }
    }

    fun nextChapter() {
        val currentChapter = _uiState.value.currentChapter
        val totalChapters = _uiState.value.totalChapters
        if (currentChapter < totalChapters) {
            loadChapter(currentChapter + 1)
        }
    }

    fun previousChapter() {
        val currentChapter = _uiState.value.currentChapter
        if (currentChapter > 1) {
            loadChapter(currentChapter - 1)
        }
    }

    fun increaseFontSize() {
        _uiState.update {
            it.copy(fontSize = (it.fontSize + 2f).coerceAtMost(32f))
        }
    }

    fun decreaseFontSize() {
        _uiState.update {
            it.copy(fontSize = (it.fontSize - 2f).coerceAtLeast(12f))
        }
    }

    fun retry() {
        loadChapter(_uiState.value.currentChapter)
    }
}
