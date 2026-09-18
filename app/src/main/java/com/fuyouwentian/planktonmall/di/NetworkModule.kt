package com.fuyouwentian.planktonmall.di

import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// 1. 配置 Json 实例
val json = Json {
    ignoreUnknownKeys = true // 核心：遇到不认识的字段不报错，直接忽略
    coerceInputValues = true // 可选：如果遇到 null 但非空字段，尝试给默认值
}

/**
 * Hilt 扫描所有 @Module，收集"如何创建依赖"的信息。查看 RepositoryModule
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder() // 2. 构建 Retrofit
        .baseUrl("https://liangchaoshun.site/api/client/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}