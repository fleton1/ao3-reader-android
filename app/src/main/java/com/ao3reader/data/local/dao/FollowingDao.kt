package com.ao3reader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ao3reader.data.local.entities.FollowingEntity
import com.ao3reader.data.local.entities.FollowingType
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowingDao {
    @Query("SELECT * FROM following WHERE id = :id")
    fun getFollowing(id: String): Flow<FollowingEntity?>

    @Query("SELECT * FROM following WHERE id = :id")
    suspend fun getFollowingOnce(id: String): FollowingEntity?

    @Query("SELECT * FROM following ORDER BY followedAt DESC")
    fun getAllFollowing(): Flow<List<FollowingEntity>>

    @Query("SELECT * FROM following WHERE type = :type ORDER BY followedAt DESC")
    fun getFollowingByType(type: FollowingType): Flow<List<FollowingEntity>>

    @Query("SELECT * FROM following WHERE hasUpdate = 1 ORDER BY lastChecked DESC")
    fun getFollowingWithUpdates(): Flow<List<FollowingEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM following WHERE id = :id)")
    suspend fun isFollowing(id: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM following WHERE id = :id)")
    fun isFollowingFlow(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowing(following: FollowingEntity)

    @Update
    suspend fun updateFollowing(following: FollowingEntity)

    @Query("UPDATE following SET lastChecked = :timestamp, lastKnownChapters = :chapters, hasUpdate = :hasUpdate WHERE id = :id")
    suspend fun updateFollowingStatus(
        id: String,
        chapters: Int,
        hasUpdate: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE following SET hasUpdate = 0 WHERE id = :id")
    suspend fun markUpdateAsRead(id: String)

    @Query("DELETE FROM following WHERE id = :id")
    suspend fun deleteFollowing(id: String)

    @Query("SELECT COUNT(*) FROM following")
    suspend fun getFollowingCount(): Int

    @Query("SELECT COUNT(*) FROM following WHERE hasUpdate = 1")
    suspend fun getUpdateCount(): Int
}
