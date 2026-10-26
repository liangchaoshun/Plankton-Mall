package com.fuyouwentian.planktonmall.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fuyouwentian.planktonmall.data.local.cart.CartDao
import com.fuyouwentian.planktonmall.data.local.cart.CartEntity
import com.fuyouwentian.planktonmall.data.local.favor.FavorDao
import com.fuyouwentian.planktonmall.data.local.favor.FavorEntity
import com.fuyouwentian.planktonmall.data.local.history.HistoryDao
import com.fuyouwentian.planktonmall.data.local.history.HistoryEntity
import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
import com.fuyouwentian.planktonmall.data.local.profile.ProfileEntity

// ⚠🌟 数据库要添加表，都要在 entities 添加，且要添加抽象方法
@Database(
    entities = [
        CartEntity::class,
        ProfileEntity::class,
        HistoryEntity::class,
        FavorEntity::class
    ],
    version = 9,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun profileDao(): ProfileDao
    abstract fun historyDao(): HistoryDao
    abstract fun favorDao(): FavorDao
}