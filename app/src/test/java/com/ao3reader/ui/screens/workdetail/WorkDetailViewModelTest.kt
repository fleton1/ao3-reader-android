package com.ao3reader.ui.screens.workdetail

import androidx.lifecycle.SavedStateHandle
import com.ao3reader.data.repository.BookmarkRepository
import com.ao3reader.data.repository.DownloadRepository
import com.ao3reader.data.repository.FollowingRepository
import com.ao3reader.data.repository.WorkRepository
import com.ao3reader.domain.models.Rating
import com.ao3reader.domain.models.Resource
import com.ao3reader.domain.models.Work
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for WorkDetailViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var workRepository: WorkRepository

    @Mock
    private lateinit var bookmarkRepository: BookmarkRepository

    @Mock
    private lateinit var downloadRepository: DownloadRepository

    @Mock
    private lateinit var followingRepository: FollowingRepository

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: WorkDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)

        savedStateHandle = SavedStateHandle(mapOf("workId" to "12345"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadWork updates uiState with work data`() = runTest {
        // Given
        val testWork = createTestWork()
        `when`(workRepository.getWork("12345", false))
            .thenReturn(flowOf(Resource.Success(testWork)))
        `when`(bookmarkRepository.isBookmarked("12345"))
            .thenReturn(flowOf(false))
        `when`(downloadRepository.isDownloaded("12345"))
            .thenReturn(flowOf(false))
        `when`(followingRepository.isFollowing("12345"))
            .thenReturn(flowOf(false))

        // When
        viewModel = WorkDetailViewModel(
            workRepository,
            bookmarkRepository,
            downloadRepository,
            followingRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNotNull(uiState.work)
        assertEquals("Test Work", uiState.work?.title)
    }

    @Test
    fun `loadWork handles error state`() = runTest {
        // Given
        val errorMessage = "Network error"
        `when`(workRepository.getWork("12345", false))
            .thenReturn(flowOf(Resource.Error(errorMessage)))
        `when`(bookmarkRepository.isBookmarked("12345"))
            .thenReturn(flowOf(false))
        `when`(downloadRepository.isDownloaded("12345"))
            .thenReturn(flowOf(false))
        `when`(followingRepository.isFollowing("12345"))
            .thenReturn(flowOf(false))

        // When
        viewModel = WorkDetailViewModel(
            workRepository,
            bookmarkRepository,
            downloadRepository,
            followingRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(errorMessage, uiState.error)
    }

    @Test
    fun `toggleBookmark calls repository`() = runTest {
        // Given
        val testWork = createTestWork()
        `when`(workRepository.getWork("12345", false))
            .thenReturn(flowOf(Resource.Success(testWork)))
        `when`(bookmarkRepository.isBookmarked("12345"))
            .thenReturn(flowOf(false))
        `when`(downloadRepository.isDownloaded("12345"))
            .thenReturn(flowOf(false))
        `when`(followingRepository.isFollowing("12345"))
            .thenReturn(flowOf(false))

        viewModel = WorkDetailViewModel(
            workRepository,
            bookmarkRepository,
            downloadRepository,
            followingRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        // When
        viewModel.toggleBookmark()
        advanceUntilIdle()

        // Then
        verify(bookmarkRepository).addBookmark("12345")
    }

    @Test
    fun `refresh reloads work data`() = runTest {
        // Given
        val testWork = createTestWork()
        `when`(workRepository.getWork("12345", false))
            .thenReturn(flowOf(Resource.Success(testWork)))
        `when`(workRepository.getWork("12345", true))
            .thenReturn(flowOf(Resource.Success(testWork)))
        `when`(bookmarkRepository.isBookmarked("12345"))
            .thenReturn(flowOf(false))
        `when`(downloadRepository.isDownloaded("12345"))
            .thenReturn(flowOf(false))
        `when`(followingRepository.isFollowing("12345"))
            .thenReturn(flowOf(false))

        viewModel = WorkDetailViewModel(
            workRepository,
            bookmarkRepository,
            downloadRepository,
            followingRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        verify(workRepository).getWork("12345", true)
    }

    private fun createTestWork() = Work(
        id = "12345",
        title = "Test Work",
        author = "Test Author",
        authorId = "author123",
        summary = "Test summary",
        rating = Rating.GENERAL,
        warnings = emptyList(),
        categories = emptyList(),
        fandoms = listOf("Test Fandom"),
        relationships = emptyList(),
        characters = emptyList(),
        additionalTags = emptyList(),
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
