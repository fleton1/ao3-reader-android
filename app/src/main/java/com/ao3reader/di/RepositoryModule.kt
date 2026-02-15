package com.ao3reader.di

import com.ao3reader.data.local.dao.BookmarkDao
import com.ao3reader.data.local.dao.ChapterDao
import com.ao3reader.data.local.dao.DownloadDao
import com.ao3reader.data.local.dao.FollowingDao
import com.ao3reader.data.local.dao.WorkDao
import com.ao3reader.data.remote.AO3Scraper
import com.ao3reader.data.repository.BookmarkRepository
import com.ao3reader.data.repository.DownloadRepository
import com.ao3reader.data.repository.FollowingRepository
import com.ao3reader.data.repository.SearchRepository
import com.ao3reader.data.repository.WorkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWorkRepository(
        workDao: WorkDao,
        chapterDao: ChapterDao,
        bookmarkDao: BookmarkDao,
        downloadDao: DownloadDao,
        followingDao: FollowingDao,
        ao3Scraper: AO3Scraper
    ): WorkRepository {
        return WorkRepository(
            workDao,
            chapterDao,
            bookmarkDao,
            downloadDao,
            followingDao,
            ao3Scraper
        )
    }

    @Provides
    @Singleton
    fun provideSearchRepository(
        workDao: WorkDao,
        ao3Scraper: AO3Scraper
    ): SearchRepository {
        return SearchRepository(workDao, ao3Scraper)
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(
        bookmarkDao: BookmarkDao,
        workDao: WorkDao
    ): BookmarkRepository {
        return BookmarkRepository(bookmarkDao, workDao)
    }

    @Provides
    @Singleton
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
        workDao: WorkDao,
        chapterDao: ChapterDao,
        ao3Scraper: AO3Scraper,
        workManagerHelper: com.ao3reader.workers.WorkManagerHelper
    ): DownloadRepository {
        return DownloadRepository(downloadDao, workDao, chapterDao, ao3Scraper, workManagerHelper)
    }

    @Provides
    @Singleton
    fun provideFollowingRepository(
        followingDao: FollowingDao,
        workDao: WorkDao,
        ao3Scraper: AO3Scraper
    ): FollowingRepository {
        return FollowingRepository(followingDao, workDao, ao3Scraper)
    }
}
