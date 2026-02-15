package com.ao3reader.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ao3reader.data.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState(isLoading = true))
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            bookmarkRepository.getAllBookmarks().collect { bookmarks ->
                _uiState.update {
                    it.copy(
                        bookmarks = bookmarks,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun removeBookmark(workId: String) {
        viewModelScope.launch {
            bookmarkRepository.removeBookmark(workId)
        }
    }
}
