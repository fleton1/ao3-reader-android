package com.ao3reader.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val name: String,
    val type: TagType,
    val count: Int = 0 // Number of works with this tag
)

enum class TagType {
    FANDOM,
    RELATIONSHIP,
    CHARACTER,
    FREEFORM,
    WARNING,
    CATEGORY,
    RATING
}
