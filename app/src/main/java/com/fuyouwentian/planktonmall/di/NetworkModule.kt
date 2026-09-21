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
    encodeDefaults = true // 即使是默认值也发送给后端
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
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder() // 2. 构建 Retrofit
        /**
         * 本地接口（略显麻烦！）
         * 真机调试的最强外挂，100% 绕过所有网络问题
         * 显示所有设备：adb devices
         * 把真机的 8068 端口，反向代理到电脑的 8068 端口：adb -s VBJ0218804010364 reverse tcp:8068 tcp:8068
         * -s 后面跟着设备序列号，该设备是 Android Studio 顶部工具栏（运行按钮左边）选中的设备。序列号：真机固定，模拟器会变但有规律
         * 用到的其他本地端口，都需要设置反向代理，如：8058 8068 等，或在项目根目录下，创建一个脚本文件
         * localhost 或 127.0.0.1 换成局域网 ip：192.168.1.8 后，不需要 adb reverse，但要设置局域网防火墙，很麻烦且不易成功
         * 注意，adb reverse 设置反向代理端口后，localhost 可以换成 127.0.0.1，但是不能换成局域网 ip
         * 注意：每次重启手机或拔掉 USB 后，都需要重新执行 adb -s VBJ0218804010364 reverse tcp:8068 tcp:8068）
         * 注意：本地后端服务器所监听的域名（hostname），需要修改为监听所有网卡：0.0.0.0
         */
        .baseUrl("http://localhost:8068/api/client/") // ✅ 真机、模拟器使用 localhost 或 127.0.0.1 加上 adb reverse 可行
        // .baseUrl("http://10.0.2.2:8068/api/client/") // ❌ 真机、模拟器使用该地址不可行，再加上 adb reverse 也不可行
//        .baseUrl("https://liangchaoshun.site/api/client/") // ✅ remote
        .client(okHttpClient) // 🌟 关键：把配置了日志拦截器的 OkHttpClient 传进去
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}