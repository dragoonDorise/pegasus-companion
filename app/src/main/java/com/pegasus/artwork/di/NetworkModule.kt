package com.pegasus.artwork.di

import com.pegasus.artwork.data.remote.RateLimiterInterceptor
import com.pegasus.artwork.data.remote.ScreenScraperApi
import com.pegasus.artwork.data.remote.ThemesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideRateLimiterInterceptor(): RateLimiterInterceptor = RateLimiterInterceptor()

    @Provides
    @Singleton
    @ApiClient
    fun provideApiOkHttpClient(rateLimiter: RateLimiterInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(rateLimiter)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @DownloadClient
    fun provideDownloadOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(@ApiClient okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://www.screenscraper.fr/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideScreenScraperApi(retrofit: Retrofit): ScreenScraperApi {
        return retrofit.create(ScreenScraperApi::class.java)
    }

    @Provides
    @Singleton
    fun provideThemesApi(@DownloadClient okHttpClient: OkHttpClient, json: Json): ThemesApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://f005.backblazeb2.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ThemesApi::class.java)
    }
}
