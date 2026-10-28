package com.fuyouwentian.planktonmall.data.local.favor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavorDao {
    @Query("SELECT * FROM favor")
    fun getAllItems(): Flow<List<FavorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavorEntity)

    // 若是复合键，则判断复合键
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMulti(items: List<FavorEntity>)

    @Query("DELETE FROM favor")
    suspend fun clearAll()

    @Query("DELETE FROM favor WHERE id IN (:ids)")
    suspend fun deleteItemsByIds(ids: List<String>)

    // 即使没有 LIMIT，只要返回类型是单对象（如 ProfileEntity），空表依然会返回 null
    // 若返回 List<ClassName> 则为 emptyList()
    @Query("SELECT * FROM favor WHERE goodsId = :id")
    suspend fun getItemByGoodsId(id: String): FavorEntity?

    @Query("SELECT * FROM favor WHERE id IN (:ids)")
    suspend fun getItemsByIds(ids: List<String>): List<FavorEntity>

    @Query("SELECT COUNT(*) FROM favor WHERE goodsId = :id")
    suspend fun getItemCountByGoodsId(id: String): Int
}
