package com.ao3reader.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "work_tag_cross_ref",
    primaryKeys = ["workId", "tagName"],
    foreignKeys = [
        ForeignKey(
            entity = WorkEntity::class,
            parentColumns = ["id"],
            childColumns = ["workId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["name"],
            childColumns = ["tagName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workId"),
        Index("tagName")
    ]
)
data class WorkTagCrossRef(
    val workId: String,
    val tagName: String
)
