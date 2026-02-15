package com.ao3reader.ui.screens.following

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ao3reader.data.repository.FollowingRepository
import com.ao3reader.workers.WorkManagerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val followingRepository: FollowingRepository,
    private val workManagerHelper: WorkManagerHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowingUiState(isLoading = true))
    val uiState: StateFlow<FollowingUiState> = _uiState.asStateFlow()

    init {
        loadFollowing()
    }

    private fun loadFollowing() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            followingRepository.getAllFollowing().collect { following ->
                _uiState.update {
                    it.copy(
                        following = following,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }

            // Trigger immediate update check using WorkManager
            // This will run in the background and send notifications
            workManagerHelper.triggerUpdateCheck()

            // Also update UI immediately
            followingRepository.checkForUpdates()
                .onSuccess { updateCount ->
                    _uiState.update { it.copy(isChecking = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            error = error.message ?: "Failed to check for updates"
                        )
                    }
                }
        }
    }

    fun unfollow(id: String) {
        viewModelScope.launch {
            followingRepository.unfollow(id)
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            followingRepository.markUpdateAsRead(id)
        }
    }
}
