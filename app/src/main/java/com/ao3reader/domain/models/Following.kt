package com.ao3reader.domain.models

import com.ao3reader.data.local.entities.FollowingEntity
import com.ao3reader.data.local.entities.FollowingType

/**
 * Domain model for a followed work or author.
 */
data class Following(
    val id: String,
    val type: FollowingType,
    val name: String,
    val work: Work?,
    val followedAt: Long,
    val lastChecked: Long?,
    val lastKnownChapters: Int,
    val hasUpdate: Boolean
)

/**
 * Extension function to convert FollowingEntity to domain Following model.
 */
fun FollowingEntity.toDomain(work: Work? = null): Following {
    return Following(
        id = id,
        type = type,
        name = name,
        work = work,
        followedAt = followedAt,
        lastChecked = lastChecked,
        lastKnownChapters = lastKnownChapters,
        hasUpdate = hasUpdate
    )
}
