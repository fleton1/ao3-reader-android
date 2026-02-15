package com.ao3reader.di

import android.content.Context
import androidx.room.Room
import com.ao3reader.data.local.AO3Database
import com.ao3reader.data.local.dao.BookmarkDao
import com.ao3reader.data.local.dao.ChapterDao
import com.ao3reader.data.local.dao.DownloadDao
import com.ao3reader.data.local.dao.FollowingDao
import com.ao3reader.data.local.dao.WorkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAO3Database(
        @ApplicationContext context: Context
    ): AO3Database {
        return Room.databaseBuilder(
            context,
            AO3Database::class.java,
            AO3Database.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkDao(database: AO3Database): WorkDao {
        return database.workDao()
    }

    @Provides
    @Singleton
    fun provideChapterDao(database: AO3Database): ChapterDao {
        return database.chapterDao()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: AO3Database): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: AO3Database): DownloadDao {
        return database.downloadDao()
    }

    @Provides
    @Singleton
    fun provideFollowingDao(database: AO3Database): FollowingDao {
        return database.followingDao()
    }
}
