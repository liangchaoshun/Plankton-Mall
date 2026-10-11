package com.fuyouwentian.planktonmall.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    /**
     * 应用级协程作用域：
     * - SupervisorJob：一个子协程失败不影响其他子协程
     * - Dispatchers.IO：适合 IO 密集任务（网络、磁盘）
     * - 随进程存活，不要手动 cancel（除非进程退出）
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
}