package com.fuyouwentian.planktonmall.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fuyouwentian.planktonmall.data.local.cart.CartDao
import com.fuyouwentian.planktonmall.data.local.cart.CartEntity

@Database(
    entities = [CartEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}
