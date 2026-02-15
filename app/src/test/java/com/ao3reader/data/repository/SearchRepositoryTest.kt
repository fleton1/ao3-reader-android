package com.ao3reader.data.repository

import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.remote.AO3Scraper
import com.ao3reader.data.remote.models.WorkDto
import com.ao3reader.domain.models.Resource
import com.ao3reader.domain.models.SearchFilters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for SearchRepository.
 */
class SearchRepositoryTest {

    @Mock
    private lateinit var workDao: WorkDao

    @Mock
    private lateinit var ao3Scraper: AO3Scraper

    private lateinit var repository: SearchRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = SearchRepository(workDao, ao3Scraper)
    }

    @Test
    fun `searchWorks returns empty list for blank query`() = runTest {
        // When
        val result = repository.searchWorks("", SearchFilters()).first()

        // Then
        assertTrue(result is Resource.Success)
        assertTrue((result as Resource.Success).data?.isEmpty() == true)
    }

    @Test
    fun `searchWorks returns results from scraper`() = runTest {
        // Given
        val query = "Harry Potter"
        val workDtos = listOf(
            createTestWorkDto("1"),
            createTestWorkDto("2")
        )
        `when`(ao3Scraper.searchWorks(query, 1)).thenReturn(Result.success(workDtos))

        // When
        val result = repository.searchWorks(query, SearchFilters(), page = 1).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(2, (result as Resource.Success).data?.size)
        verify(ao3Scraper).searchWorks(query, 1)
        verify(workDao).insertWorks(workDtos.map { it.toEntity() })
    }

    @Test
    fun `searchWorks returns error when scraper fails`() = runTest {
        // Given
        val query = "Test"
        val errorMessage = "Network error"
        `when`(ao3Scraper.searchWorks(query, 1)).thenReturn(Result.failure(Exception(errorMessage)))

        // When
        val result = repository.searchWorks(query, SearchFilters()).first()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }

    private fun createTestWorkDto(id: String) = WorkDto(
        id = id,
        title = "Test Work $id",
        author = "Test Author",
        authorId = "author123",
        summary = "Test summary",
        rating = "General",
        warnings = "No Warnings",
        category = "Gen",
        fandom = "Test Fandom",
        relationships = "",
        characters = "",
        additionalTags = "",
        language = "English",
        publishedDate = System.currentTimeMillis(),
        updatedDate = System.currentTimeMillis(),
        words = 5000,
        currentChapters = 5,
        totalChapters = "10",
        kudos = 100,
        bookmarksCount = 50,
        hits = 1000,
        isComplete = false
    )
}
