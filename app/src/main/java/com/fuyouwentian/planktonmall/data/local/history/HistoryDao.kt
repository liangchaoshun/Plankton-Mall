package com.fuyouwentian.planktonmall.data.local.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryEntity)

    @Query("DELETE FROM history WHERE id IN (:ids)")
    suspend fun deleteItemsByIds(ids: List<String>)

    @Query("SELECT * FROM history")
    suspend fun getAllItems(): List<HistoryEntity> // 空表时，返回 emptyList()
}