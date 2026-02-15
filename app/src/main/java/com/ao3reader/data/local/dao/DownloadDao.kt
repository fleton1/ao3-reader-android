package com.ao3reader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ao3reader.data.local.entities.DownloadEntity
import com.ao3reader.data.local.entities.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE workId = :workId")
    fun getDownload(workId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE workId = :workId")
    suspend fun getDownloadOnce(workId: String): DownloadEntity?

    @Query("SELECT * FROM downloads ORDER BY startedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY startedAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'IN_PROGRESS'")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE workId = :workId AND status = 'COMPLETED')")
    suspend fun isDownloaded(workId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE workId = :workId AND status = 'COMPLETED')")
    fun isDownloadedFlow(workId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status, downloadedChapters = :downloadedChapters WHERE workId = :workId")
    suspend fun updateDownloadProgress(workId: String, status: DownloadStatus, downloadedChapters: Int)

    @Query("UPDATE downloads SET status = :status, completedAt = :completedAt WHERE workId = :workId")
    suspend fun completeDownload(
        workId: String,
        status: DownloadStatus,
        completedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE downloads SET status = 'FAILED', errorMessage = :errorMessage WHERE workId = :workId")
    suspend fun failDownload(workId: String, errorMessage: String)

    @Query("DELETE FROM downloads WHERE workId = :workId")
    suspend fun deleteDownload(workId: String)

    @Query("DELETE FROM downloads WHERE status = 'FAILED' OR status = 'CANCELLED'")
    suspend fun clearFailedDownloads()

    @Query("SELECT COUNT(*) FROM downloads WHERE status = 'COMPLETED'")
    suspend fun getCompletedDownloadCount(): Int
}
