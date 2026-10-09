package com.fuyouwentian.planktonmall.data.local.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CartDao {
    @Query("SELECT * FROM cart")
    suspend fun getAll(): List<CartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CartEntity>)

    @Update
    suspend fun update(item: CartEntity)

    @Query("DELETE FROM cart")
    suspend fun clearAll()

    @Query("DELETE FROM cart WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}