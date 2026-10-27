package com.fuyouwentian.planktonmall.data.local.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {
    // 若是复合键，则判断复合键
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryEntity)

    // 若是复合键，则判断复合键
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMulti(items: List<HistoryEntity>)

    @Query("DELETE FROM history WHERE goodsId IN (:ids)")
    suspend fun deleteItemsByIds(ids: List<String>)

    @Query("SELECT * FROM history WHERE goodsId IN (:ids)")
    suspend fun getItemsByIds(ids: List<String>): List<HistoryEntity>

    // 即使没有 LIMIT，只要返回类型是单对象（如 ProfileEntity），空表依然会返回 null
    // 若返回 List<ClassName> 则为 emptyList()
    @Query("SELECT * FROM history")
    suspend fun getAllItems(): List<HistoryEntity>
}