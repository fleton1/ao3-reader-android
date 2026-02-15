package com.ao3reader.data.repository

import com.ao3reader.data.local.dao.BookmarkDao
import com.ao3reader.data.local.dao.ChapterDao
import com.ao3reader.data.local.dao.DownloadDao
import com.ao3reader.data.local.dao.FollowingDao
import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.local.entities.WorkEntity
import com.ao3reader.data.remote.AO3Scraper
import com.ao3reader.data.remote.models.WorkDto
import com.ao3reader.domain.models.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
 * Unit tests for WorkRepository.
 * Tests cache-first strategy and network fallback.
 */
class WorkRepositoryTest {

    @Mock
    private lateinit var workDao: WorkDao

    @Mock
    private lateinit var chapterDao: ChapterDao

    @Mock
    private lateinit var bookmarkDao: BookmarkDao

    @Mock
    private lateinit var downloadDao: DownloadDao

    @Mock
    private lateinit var followingDao: FollowingDao

    @Mock
    private lateinit var ao3Scraper: AO3Scraper

    private lateinit var repository: WorkRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = WorkRepository(
            workDao,
            chapterDao,
            bookmarkDao,
            downloadDao,
            followingDao,
            ao3Scraper
        )
    }

    @Test
    fun `getWork returns cached work when available`() = runTest {
        // Given
        val workId = "12345"
        val cachedWork = createTestWorkEntity(workId)
        `when`(workDao.getWork(workId)).thenReturn(flowOf(cachedWork))
        `when`(bookmarkDao.isBookmarked(workId)).thenReturn(false)
        `when`(downloadDao.isDownloaded(workId)).thenReturn(false)
        `when`(followingDao.isFollowing(workId)).thenReturn(false)

        // When
        val result = repository.getWork(workId, forceRefresh = false).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(workId, (result as Resource.Success).data?.id)
        verify(workDao).getWork(workId)
    }

    @Test
    fun `getWork fetches from network when cache is empty`() = runTest {
        // Given
        val workId = "12345"
        val workDto = createTestWorkDto(workId)
        `when`(workDao.getWork(workId)).thenReturn(flowOf(null))
        `when`(ao3Scraper.getWork(workId)).thenReturn(Result.success(workDto))
        `when`(bookmarkDao.isBookmarked(workId)).thenReturn(false)
        `when`(downloadDao.isDownloaded(workId)).thenReturn(false)
        `when`(followingDao.isFollowing(workId)).thenReturn(false)

        // When
        val result = repository.getWork(workId, forceRefresh = false).first()

        // Then
        assertTrue(result is Resource.Success)
        verify(ao3Scraper).getWork(workId)
        verify(workDao).insertWork(workDto.toEntity())
    }

    @Test
    fun `getWork returns error when network fails`() = runTest {
        // Given
        val workId = "12345"
        val errorMessage = "Network error"
        `when`(workDao.getWork(workId)).thenReturn(flowOf(null))
        `when`(ao3Scraper.getWork(workId)).thenReturn(Result.failure(Exception(errorMessage)))

        // When
        val result = repository.getWork(workId, forceRefresh = false).first()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }

    @Test
    fun `getWork force refresh bypasses cache`() = runTest {
        // Given
        val workId = "12345"
        val workDto = createTestWorkDto(workId)
        `when`(ao3Scraper.getWork(workId)).thenReturn(Result.success(workDto))
        `when`(bookmarkDao.isBookmarked(workId)).thenReturn(false)
        `when`(downloadDao.isDownloaded(workId)).thenReturn(false)
        `when`(followingDao.isFollowing(workId)).thenReturn(false)

        // When
        repository.getWork(workId, forceRefresh = true).first()

        // Then
        verify(ao3Scraper).getWork(workId)
        verify(workDao).insertWork(workDto.toEntity())
    }

    // Helper functions
    private fun createTestWorkEntity(id: String) = WorkEntity(
        id = id,
        title = "Test Work",
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
        isComplete = false,
        isSeries = false,
        seriesName = null,
        seriesPart = null
    )

    private fun createTestWorkDto(id: String) = WorkDto(
        id = id,
        title = "Test Work",
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
