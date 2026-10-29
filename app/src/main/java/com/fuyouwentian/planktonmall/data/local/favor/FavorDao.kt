package com.fuyouwentian.planktonmall.data.local.favor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavorDao {
    @Query("SELECT * FROM favor WHERE userId = :userId")
    fun getAllItems(userId: String): Flow<List<FavorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavorEntity)

    // 若是复合键，则判断复合键
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMulti(items: List<FavorEntity>)

    // 清空 favor 数据表，无论是谁的数据（慎用）
    @Query("DELETE FROM favor")
    suspend fun clearAll()

    // 删除某用户的所有记录
    @Query("DELETE FROM favor WHERE userId = :userId")
    suspend fun clearItemsByUserId(userId: String)

    @Query("DELETE FROM favor WHERE userId = :userId AND goodsId IN (:goodsIds)")
    suspend fun deleteItems(userId: String, goodsIds: List<String>)

    // 即使没有 LIMIT，只要返回类型是单对象，空表会返回 null
    // 若返回 List<ClassName> 则为 emptyList()
    @Query("SELECT * FROM favor WHERE userId = :userId AND goodsId = :goodsId LIMIT 1")
    suspend fun getItem(userId: String, goodsId: String): FavorEntity?

    // 返回 Boolean，只要存在就返回 true
    @Query("SELECT EXISTS(SELECT 1 FROM favor WHERE userId = :userId AND goodsId = :goodsId)")
    suspend fun isFavorite(userId: String, goodsId: String): Boolean

    @Query("SELECT COUNT(*) FROM favor WHERE userId = :userId")
    suspend fun getCount(userId: String): Int

}
