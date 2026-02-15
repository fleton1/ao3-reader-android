package com.ao3reader.data.repository

import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.remote.AO3Scraper
import com.ao3reader.domain.models.Resource
import com.ao3reader.domain.models.SearchFilters
import com.ao3reader.domain.models.Work
import com.ao3reader.domain.models.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for searching works.
 */
@Singleton
class SearchRepository @Inject constructor(
    private val workDao: WorkDao,
    private val ao3Scraper: AO3Scraper
) {
    /**
     * Searches for works using the provided query and filters.
     * Always fetches from network for fresh results.
     */
    fun searchWorks(
        query: String,
        filters: SearchFilters = SearchFilters(),
        page: Int = 1
    ): Flow<Resource<List<Work>>> = flow {
        emit(Resource.Loading())

        // If query is empty, return empty list
        if (query.isBlank()) {
            emit(Resource.Success(emptyList()))
            return@flow
        }

        // Fetch from network
        ao3Scraper.searchWorks(query, page)
            .onSuccess { workDtos ->
                // Cache the results
                val entities = workDtos.map { it.toEntity() }
                workDao.insertWorks(entities)

                // Return as domain models
                val works = entities.map { it.toDomain() }
                emit(Resource.Success(works))
            }
            .onFailure { e ->
                emit(Resource.Error(e.message ?: "Search failed"))
            }
    }

    /**
     * Searches locally cached works.
     * Useful for offline mode.
     */
    fun searchCachedWorks(
        query: String,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Work>> = flow {
        workDao.searchWorks(query, limit, offset).collect { works ->
            emit(works.map { it.toDomain() })
        }
    }
}
