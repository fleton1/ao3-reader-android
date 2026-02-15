package com.ao3reader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ao3reader.data.local.entities.WorkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkDao {
    @Query("SELECT * FROM works WHERE id = :workId")
    fun getWork(workId: String): Flow<WorkEntity?>

    @Query("SELECT * FROM works WHERE id = :workId")
    suspend fun getWorkOnce(workId: String): WorkEntity?

    @Query("SELECT * FROM works ORDER BY cachedAt DESC LIMIT :limit OFFSET :offset")
    fun getAllWorks(limit: Int = 50, offset: Int = 0): Flow<List<WorkEntity>>

    @Query("SELECT * FROM works WHERE author = :authorName ORDER BY updatedDate DESC")
    fun getWorksByAuthor(authorName: String): Flow<List<WorkEntity>>

    @Query("""
        SELECT * FROM works
        WHERE title LIKE '%' || :query || '%'
        OR author LIKE '%' || :query || '%'
        OR summary LIKE '%' || :query || '%'
        ORDER BY updatedDate DESC
        LIMIT :limit OFFSET :offset
    """)
    fun searchWorks(query: String, limit: Int = 50, offset: Int = 0): Flow<List<WorkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWork(work: WorkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorks(works: List<WorkEntity>)

    @Update
    suspend fun updateWork(work: WorkEntity)

    @Query("DELETE FROM works WHERE id = :workId")
    suspend fun deleteWork(workId: String)

    @Query("DELETE FROM works WHERE cachedAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)

    @Query("SELECT COUNT(*) FROM works")
    suspend fun getWorkCount(): Int
}
