package com.ao3reader.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ao3reader.data.repository.BookmarkRepository
import com.ao3reader.data.repository.DownloadRepository
import com.ao3reader.data.repository.FollowingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val downloadRepository: DownloadRepository,
    private val followingRepository: FollowingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val bookmarksCount = bookmarkRepository.getBookmarkCount()
            val updateCount = followingRepository.getUpdateCount()

            downloadRepository.getCompletedDownloads().collect { downloads ->
                _uiState.update { state ->
                    state.copy(
                        recentBookmarksCount = bookmarksCount,
                        downloadsCount = downloads.size,
                        updateCount = updateCount,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh() {
        loadStats()
    }
}
