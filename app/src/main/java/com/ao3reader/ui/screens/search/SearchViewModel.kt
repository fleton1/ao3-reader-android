package com.ao3reader.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ao3reader.data.repository.SearchRepository
import com.ao3reader.domain.models.Resource
import com.ao3reader.domain.models.SearchFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search(page: Int = 1) {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a search query") }
            return
        }

        viewModelScope.launch {
            searchRepository.searchWorks(
                query = query,
                filters = _uiState.value.filters,
                page = page
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                results = resource.data ?: emptyList(),
                                isLoading = false,
                                hasSearched = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Search failed",
                                hasSearched = true
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearResults() {
        _uiState.update { SearchUiState() }
    }

    fun updateFilters(filters: SearchFilters) {
        _uiState.update { it.copy(filters = filters) }
    }
}
