package com.ao3reader.data.repository

import com.ao3reader.data.local.dao.FollowingDao
import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.local.entities.FollowingEntity
import com.ao3reader.data.local.entities.FollowingType
import com.ao3reader.data.remote.AO3Scraper
import com.ao3reader.domain.models.Following
import com.ao3reader.domain.models.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing followed works and authors.
 */
@Singleton
class FollowingRepository @Inject constructor(
    private val followingDao: FollowingDao,
    private val workDao: WorkDao,
    private val ao3Scraper: AO3Scraper
) {
    /**
     * Gets all followed works/authors with their associated data.
     */
    fun getAllFollowing(): Flow<List<Following>> {
        return followingDao.getAllFollowing().map { followingList ->
            followingList.map { following ->
                val work = if (following.type == FollowingType.WORK) {
                    workDao.getWorkOnce(following.id)?.toDomain(isFollowing = true)
                } else {
                    null
                }
                following.toDomain(work)
            }
        }
    }

    /**
     * Gets followed items with updates available.
     */
    fun getFollowingWithUpdates(): Flow<List<Following>> {
        return followingDao.getFollowingWithUpdates().map { followingList ->
            followingList.map { following ->
                val work = if (following.type == FollowingType.WORK) {
                    workDao.getWorkOnce(following.id)?.toDomain(isFollowing = true)
                } else {
                    null
                }
                following.toDomain(work)
            }
        }
    }

    /**
     * Gets followed works only.
     */
    fun getFollowedWorks(): Flow<List<Following>> {
        return followingDao.getFollowingByType(FollowingType.WORK).map { followingList ->
            followingList.map { following ->
                val work = workDao.getWorkOnce(following.id)?.toDomain(isFollowing = true)
                following.toDomain(work)
            }
        }
    }

    /**
     * Gets followed authors only.
     */
    fun getFollowedAuthors(): Flow<List<Following>> {
        return followingDao.getFollowingByType(FollowingType.AUTHOR).map { followingList ->
            followingList.map { following ->
                following.toDomain()
            }
        }
    }

    /**
     * Checks if a work/author is being followed.
     */
    fun isFollowing(id: String): Flow<Boolean> {
        return followingDao.isFollowingFlow(id)
    }

    /**
     * Follows a work.
     */
    suspend fun followWork(workId: String, workTitle: String, currentChapters: Int) {
        val following = FollowingEntity(
            id = workId,
            type = FollowingType.WORK,
            name = workTitle,
            lastKnownChapters = currentChapters
        )
        followingDao.insertFollowing(following)
    }

    /**
     * Follows an author.
     */
    suspend fun followAuthor(authorId: String, authorName: String) {
        val following = FollowingEntity(
            id = authorId,
            type = FollowingType.AUTHOR,
            name = authorName
        )
        followingDao.insertFollowing(following)
    }

    /**
     * Unfollows a work or author.
     */
    suspend fun unfollow(id: String) {
        followingDao.deleteFollowing(id)
    }

    /**
     * Checks for updates to followed works.
     * Fetches latest work info from AO3 and compares chapter counts.
     */
    suspend fun checkForUpdates(): Result<Int> {
        return try {
            val followedWorks = followingDao.getFollowingByType(FollowingType.WORK)
            var updateCount = 0

            followedWorks.collect { followingList ->
                followingList.forEach { following ->
                    try {
                        // Fetch latest work info
                        ao3Scraper.getWork(following.id)
                            .onSuccess { workDto ->
                                val hasUpdate = workDto.currentChapters > following.lastKnownChapters

                                // Update following status
                                followingDao.updateFollowingStatus(
                                    id = following.id,
                                    chapters = workDto.currentChapters,
                                    hasUpdate = hasUpdate
                                )

                                if (hasUpdate) {
                                    updateCount++
                                }

                                // Cache the updated work
                                workDao.insertWork(workDto.toEntity())
                            }
                            .onFailure { e ->
                                // Log error but continue checking other works
                                e.printStackTrace()
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            Result.success(updateCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marks an update as read/seen.
     */
    suspend fun markUpdateAsRead(id: String) {
        followingDao.markUpdateAsRead(id)
    }

    /**
     * Gets the count of items with updates.
     */
    suspend fun getUpdateCount(): Int {
        return followingDao.getUpdateCount()
    }
}
