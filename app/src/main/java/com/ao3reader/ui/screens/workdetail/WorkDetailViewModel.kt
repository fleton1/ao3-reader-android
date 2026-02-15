package com.ao3reader.ui.screens.workdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ao3reader.data.repository.BookmarkRepository
import com.ao3reader.data.repository.DownloadRepository
import com.ao3reader.data.repository.FollowingRepository
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
class WorkDetailViewModel @Inject constructor(
    private val workRepository: WorkRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val downloadRepository: DownloadRepository,
    private val followingRepository: FollowingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workId: String = checkNotNull(savedStateHandle["workId"])

    private val _uiState = MutableStateFlow(WorkDetailUiState(isLoading = true))
    val uiState: StateFlow<WorkDetailUiState> = _uiState.asStateFlow()

    init {
        loadWork()
        observeBookmarkStatus()
        observeDownloadStatus()
        observeFollowingStatus()
    }

    private fun loadWork(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            workRepository.getWork(workId, forceRefresh).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                work = resource.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Unknown error"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeBookmarkStatus() {
        viewModelScope.launch {
            bookmarkRepository.isBookmarked(workId).collect { isBookmarked ->
                _uiState.update { it.copy(isBookmarked = isBookmarked) }
            }
        }
    }

    private fun observeDownloadStatus() {
        viewModelScope.launch {
            downloadRepository.isDownloaded(workId).collect { isDownloaded ->
                _uiState.update { it.copy(isDownloaded = isDownloaded) }
            }
        }
    }

    private fun observeFollowingStatus() {
        viewModelScope.launch {
            followingRepository.isFollowing(workId).collect { isFollowing ->
                _uiState.update { it.copy(isFollowing = isFollowing) }
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val work = _uiState.value.work ?: return@launch
            if (_uiState.value.isBookmarked) {
                bookmarkRepository.removeBookmark(workId)
            } else {
                bookmarkRepository.addBookmark(workId)
            }
        }
    }

    fun downloadWork() {
        viewModelScope.launch {
            val work = _uiState.value.work ?: return@launch
            _uiState.update { it.copy(isDownloading = true) }

            // Start background download with WorkManager
            val workRequestId = downloadRepository.startDownload(
                workId = workId,
                workTitle = work.title,
                totalChapters = work.currentChapters
            )

            // Observe download progress
            downloadRepository.getDownloadProgress(workRequestId).collect { progress ->
                when (progress) {
                    is com.ao3reader.workers.DownloadProgress.InProgress -> {
                        _uiState.update {
                            it.copy(
                                isDownloading = true,
                                downloadProgress = progress.progress
                            )
                        }
                    }
                    is com.ao3reader.workers.DownloadProgress.Completed -> {
                        _uiState.update {
                            it.copy(
                                isDownloading = false,
                                downloadProgress = 1f
                            )
                        }
                    }
                    is com.ao3reader.workers.DownloadProgress.Failed -> {
                        _uiState.update {
                            it.copy(
                                isDownloading = false,
                                error = progress.error
                            )
                        }
                    }
                    is com.ao3reader.workers.DownloadProgress.Cancelled -> {
                        _uiState.update { it.copy(isDownloading = false) }
                    }
                    is com.ao3reader.workers.DownloadProgress.Pending -> {
                        // Keep downloading state
                    }
                }
            }
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            val work = _uiState.value.work ?: return@launch
            if (_uiState.value.isFollowing) {
                followingRepository.unfollow(workId)
            } else {
                followingRepository.followWork(workId, work.title, work.currentChapters)
            }
        }
    }

    fun refresh() {
        loadWork(forceRefresh = true)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
