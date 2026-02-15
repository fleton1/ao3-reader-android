package com.ao3reader.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "following")
data class FollowingEntity(
    @PrimaryKey
    val id: String, // workId or authorId depending on type
    val type: FollowingType,
    val name: String, // Work title or author name
    val followedAt: Long = System.currentTimeMillis(),
    val lastChecked: Long? = null,
    val lastKnownChapters: Int = 0,
    val hasUpdate: Boolean = false
)

enum class FollowingType {
    WORK,
    AUTHOR
}
