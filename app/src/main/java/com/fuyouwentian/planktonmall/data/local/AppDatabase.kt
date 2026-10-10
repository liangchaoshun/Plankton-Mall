package com.fuyouwentian.planktonmall.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fuyouwentian.planktonmall.data.local.cart.CartDao
import com.fuyouwentian.planktonmall.data.local.cart.CartEntity
import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
import com.fuyouwentian.planktonmall.data.local.profile.ProfileEntity

@Database(
    entities = [CartEntity::class, ProfileEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun profileDao(): ProfileDao
}
