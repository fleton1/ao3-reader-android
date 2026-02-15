package com.ao3reader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ao3reader.data.local.entities.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE workId = :workId")
    fun getBookmark(workId: String): Flow<BookmarkEntity?>

    @Query("SELECT * FROM bookmarks WHERE workId = :workId")
    suspend fun getBookmarkOnce(workId: String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks ORDER BY lastReadAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC LIMIT :limit OFFSET :offset")
    fun getBookmarksPaged(limit: Int = 20, offset: Int = 0): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE workId = :workId)")
    suspend fun isBookmarked(workId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE workId = :workId)")
    fun isBookmarkedFlow(workId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Query("UPDATE bookmarks SET currentChapter = :chapter, scrollPosition = :scrollPosition, progress = :progress, lastReadAt = :timestamp WHERE workId = :workId")
    suspend fun updateReadingProgress(
        workId: String,
        chapter: Int,
        scrollPosition: Int,
        progress: Float,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM bookmarks WHERE workId = :workId")
    suspend fun deleteBookmark(workId: String)

    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun getBookmarkCount(): Int
}
