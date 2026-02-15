package com.ao3reader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ao3reader.data.local.dao.BookmarkDao
import com.ao3reader.data.local.dao.ChapterDao
import com.ao3reader.data.local.dao.DownloadDao
import com.ao3reader.data.local.dao.FollowingDao
import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.local.entities.BookmarkEntity
import com.ao3reader.data.local.entities.ChapterEntity
import com.ao3reader.data.local.entities.DownloadEntity
import com.ao3reader.data.local.entities.FollowingEntity
import com.ao3reader.data.local.entities.TagEntity
import com.ao3reader.data.local.entities.WorkEntity
import com.ao3reader.data.local.entities.WorkTagCrossRef

@Database(
    entities = [
        WorkEntity::class,
        ChapterEntity::class,
        BookmarkEntity::class,
        DownloadEntity::class,
        FollowingEntity::class,
        TagEntity::class,
        WorkTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AO3Database : RoomDatabase() {
    abstract fun workDao(): WorkDao
    abstract fun chapterDao(): ChapterDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun downloadDao(): DownloadDao
    abstract fun followingDao(): FollowingDao

    companion object {
        const val DATABASE_NAME = "ao3_reader_db"
    }
}
