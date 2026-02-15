package com.ao3reader.di

import com.ao3reader.data.remote.AO3Scraper
import com.ao3reader.data.remote.RateLimiter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRateLimiter(): RateLimiter {
        return RateLimiter()
    }

    @Provides
    @Singleton
    fun provideAO3Scraper(
        okHttpClient: OkHttpClient,
        rateLimiter: RateLimiter
    ): AO3Scraper {
        return AO3Scraper(okHttpClient, rateLimiter)
    }
}
