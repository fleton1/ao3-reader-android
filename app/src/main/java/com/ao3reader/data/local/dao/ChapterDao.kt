package com.ao3reader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ao3reader.data.local.entities.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    fun getChapter(chapterId: String): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterOnce(chapterId: String): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE workId = :workId AND chapterNumber = :chapterNumber")
    fun getChapterByNumber(workId: String, chapterNumber: Int): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE workId = :workId ORDER BY chapterNumber ASC")
    fun getChaptersForWork(workId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE workId = :workId ORDER BY chapterNumber ASC")
    suspend fun getChaptersForWorkOnce(workId: String): List<ChapterEntity>

    @Query("SELECT COUNT(*) FROM chapters WHERE workId = :workId")
    suspend fun getChapterCount(workId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM chapters WHERE workId = :workId AND chapterNumber = :chapterNumber)")
    suspend fun isChapterCached(workId: String, chapterNumber: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun deleteChapter(chapterId: String)

    @Query("DELETE FROM chapters WHERE workId = :workId")
    suspend fun deleteChaptersForWork(workId: String)

    @Query("DELETE FROM chapters WHERE cachedAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)
}
