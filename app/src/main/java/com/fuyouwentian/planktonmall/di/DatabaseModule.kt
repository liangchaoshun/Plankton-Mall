package com.fuyouwentian.planktonmall.di

import android.content.Context
import androidx.room.Room
import com.fuyouwentian.planktonmall.data.local.AppDatabase
import com.fuyouwentian.planktonmall.data.local.cart.CartDao
import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "app_database"
            )
            // 🔥 注意：开发阶段可用，会清空数据；生产环境需手写 Migration
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao()

    @Provides
    fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()
}
