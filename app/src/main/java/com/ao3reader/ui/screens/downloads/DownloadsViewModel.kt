package com.ao3reader.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ao3reader.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState(isLoading = true))
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        loadDownloads()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            downloadRepository.getCompletedDownloads().collect { downloads ->
                _uiState.update {
                    it.copy(
                        downloads = downloads,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteDownload(workId: String) {
        viewModelScope.launch {
            downloadRepository.deleteDownload(workId)
        }
    }
}
