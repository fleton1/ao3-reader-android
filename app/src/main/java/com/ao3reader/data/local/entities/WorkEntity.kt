package com.ao3reader.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "works")
data class WorkEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String,
    val authorId: String?,
    val summary: String,
    val rating: String,
    val warnings: String,
    val category: String,
    val fandom: String,
    val relationships: String,
    val characters: String,
    val additionalTags: String,
    val language: String,
    val publishedDate: Long,
    val updatedDate: Long,
    val words: Int,
    val currentChapters: Int,
    val totalChapters: String, // Can be "?" for ongoing works
    val kudos: Int,
    val bookmarksCount: Int,
    val hits: Int,
    val isComplete: Boolean,
    val isSeries: Boolean,
    val seriesName: String?,
    val seriesPart: Int?,
    val cachedAt: Long = System.currentTimeMillis()
)
