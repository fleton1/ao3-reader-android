package com.ao3reader.data.repository

import com.ao3reader.data.local.dao.BookmarkDao
import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.local.entities.BookmarkEntity
import com.ao3reader.domain.models.Bookmark
import com.ao3reader.domain.models.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing bookmarks and reading progress.
 */
@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val workDao: WorkDao
) {
    /**
     * Gets all bookmarks with their associated works.
     */
    fun getAllBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { bookmarks ->
            bookmarks.map { bookmark ->
                val work = workDao.getWorkOnce(bookmark.workId)?.toDomain(isBookmarked = true)
                bookmark.toDomain(work)
            }
        }
    }

    /**
     * Gets a specific bookmark.
     */
    fun getBookmark(workId: String): Flow<Bookmark?> {
        return bookmarkDao.getBookmark(workId).map { bookmark ->
            bookmark?.let {
                val work = workDao.getWorkOnce(workId)?.toDomain(isBookmarked = true)
                it.toDomain(work)
            }
        }
    }

    /**
     * Checks if a work is bookmarked.
     */
    fun isBookmarked(workId: String): Flow<Boolean> {
        return bookmarkDao.isBookmarkedFlow(workId)
    }

    /**
     * Adds a bookmark for a work.
     */
    suspend fun addBookmark(workId: String, notes: String? = null) {
        val bookmark = BookmarkEntity(
            workId = workId,
            notes = notes
        )
        bookmarkDao.insertBookmark(bookmark)
    }

    /**
     * Removes a bookmark.
     */
    suspend fun removeBookmark(workId: String) {
        bookmarkDao.deleteBookmark(workId)
    }

    /**
     * Updates reading progress for a bookmarked work.
     */
    suspend fun updateReadingProgress(
        workId: String,
        currentChapter: Int,
        scrollPosition: Int,
        progress: Float
    ) {
        bookmarkDao.updateReadingProgress(
            workId = workId,
            chapter = currentChapter,
            scrollPosition = scrollPosition,
            progress = progress
        )
    }

    /**
     * Gets the total number of bookmarks.
     */
    suspend fun getBookmarkCount(): Int {
        return bookmarkDao.getBookmarkCount()
    }
}
