package com.ao3reader.data.repository

import com.ao3reader.data.local.dao.BookmarkDao
import com.ao3reader.data.local.dao.ChapterDao
import com.ao3reader.data.local.dao.DownloadDao
import com.ao3reader.data.local.dao.FollowingDao
import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.remote.AO3Scraper
import com.ao3reader.domain.models.Chapter
import com.ao3reader.domain.models.Resource
import com.ao3reader.domain.models.Work
import com.ao3reader.domain.models.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing work data.
 * Implements cache-first strategy: check local database first, then fetch from network.
 */
@Singleton
class WorkRepository @Inject constructor(
    private val workDao: WorkDao,
    private val chapterDao: ChapterDao,
    private val bookmarkDao: BookmarkDao,
    private val downloadDao: DownloadDao,
    private val followingDao: FollowingDao,
    private val ao3Scraper: AO3Scraper
) {
    /**
     * Gets a work by ID. Checks cache first, then network.
     * Combines with bookmark/download/following status.
     */
    fun getWork(workId: String, forceRefresh: Boolean = false): Flow<Resource<Work>> = flow {
        emit(Resource.Loading())

        // Check cache first unless force refresh
        if (!forceRefresh) {
            workDao.getWork(workId).collect { workEntity ->
                if (workEntity != null) {
                    // Combine with bookmark/download/following status
                    val isBookmarked = bookmarkDao.isBookmarked(workId)
                    val isDownloaded = downloadDao.isDownloaded(workId)
                    val isFollowing = followingDao.isFollowing(workId)

                    val work = workEntity.toDomain(
                        isBookmarked = isBookmarked,
                        isDownloaded = isDownloaded,
                        isFollowing = isFollowing
                    )
                    emit(Resource.Success(work))
                    return@collect
                }
            }
        }

        // Fetch from network
        ao3Scraper.getWork(workId)
            .onSuccess { workDto ->
                val entity = workDto.toEntity()
                workDao.insertWork(entity)

                val isBookmarked = bookmarkDao.isBookmarked(workId)
                val isDownloaded = downloadDao.isDownloaded(workId)
                val isFollowing = followingDao.isFollowing(workId)

                val work = entity.toDomain(
                    isBookmarked = isBookmarked,
                    isDownloaded = isDownloaded,
                    isFollowing = isFollowing
                )
                emit(Resource.Success(work))
            }
            .onFailure { e ->
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
    }

    /**
     * Gets a chapter by work ID and chapter number.
     * Checks cache first, then network.
     */
    fun getChapter(workId: String, chapterNumber: Int, forceRefresh: Boolean = false): Flow<Resource<Chapter>> = flow {
        emit(Resource.Loading())

        // Check cache first unless force refresh
        if (!forceRefresh) {
            val cachedChapter = chapterDao.getChapterByNumber(workId, chapterNumber)
            cachedChapter.collect { chapterEntity ->
                if (chapterEntity != null) {
                    emit(Resource.Success(chapterEntity.toDomain()))
                    return@collect
                }
            }
        }

        // Fetch from network
        ao3Scraper.getChapter(workId, chapterNumber)
            .onSuccess { chapterDto ->
                val entity = chapterDto.toEntity()
                chapterDao.insertChapter(entity)
                emit(Resource.Success(entity.toDomain()))
            }
            .onFailure { e ->
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
    }

    /**
     * Gets all chapters for a work.
     */
    fun getChaptersForWork(workId: String): Flow<List<Chapter>> = flow {
        chapterDao.getChaptersForWork(workId).collect { chapters ->
            emit(chapters.map { it.toDomain() })
        }
    }

    /**
     * Gets works by author name.
     */
    fun getWorksByAuthor(authorName: String): Flow<List<Work>> = flow {
        workDao.getWorksByAuthor(authorName).collect { works ->
            emit(works.map { it.toDomain() })
        }
    }

    /**
     * Downloads all chapters for a work.
     */
    suspend fun downloadAllChapters(workId: String): Result<List<Chapter>> {
        return try {
            ao3Scraper.getAllChapters(workId)
                .onSuccess { chapterDtos ->
                    val entities = chapterDtos.map { it.toEntity() }
                    chapterDao.insertChapters(entities)
                }
                .map { chapterDtos ->
                    chapterDtos.map { it.toEntity().toDomain() }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clears old cached works (older than specified timestamp).
     */
    suspend fun clearOldCache(timestamp: Long) {
        workDao.deleteOldCache(timestamp)
        chapterDao.deleteOldCache(timestamp)
    }
}
