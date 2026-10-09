package com.fuyouwentian.planktonmall.di

import android.content.Context
import androidx.room.Room
import com.fuyouwentian.planktonmall.data.local.AppDatabase
import com.fuyouwentian.planktonmall.data.local.cart.CartDao
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao()
}