package com.fuyouwentian.planktonmall.data.local.history

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface HistoryDao {
    // 即使没有 LIMIT，只要返回类型是单对象（如 *Entity），空表依然会返回 null
    // 若返回 List<ClassName> 则为 emptyList()
    @Query("SELECT * FROM history WHERE userId = :userId")
    suspend fun getRecords(userId: String): List<HistoryEntity>

    // 若是复合键，则判断复合键
    @Upsert
    suspend fun upsert(record: HistoryEntity)

    // 若是复合键，则判断复合键
    @Upsert
    suspend fun upsertMulti(records: List<HistoryEntity>)

    // 相同商品，不同用户、日期
    @Query("SELECT * FROM history WHERE goodsId IN(:goodsId)")
    suspend fun getRecordsByGoodsIds(goodsId: List<String>): List<HistoryEntity>

    // 删除某用户的所有记录
    @Query("DELETE FROM history WHERE userId = :userId")
    suspend fun deleteRecordsByUser(userId: String)

    // 通过索引删除一条记录
    @Query("DELETE FROM history WHERE userId = :userId AND goodsId = :goodsId AND createDate = :createDate")
    suspend fun deleteRecordByIndices(userId: String, goodsId: String, createDate: String)

    // 删除某一天的所有记录
    @Query("DELETE FROM history WHERE userId = :userId AND createDate = :createDate")
    suspend fun deleteRecordsByDate(userId: String, createDate: String)

    // 返回 Boolean，只要存在就返回 true
    @Query("SELECT EXISTS(SELECT 1 FROM history WHERE userId = :userId AND goodsId = :goodsId)")
    suspend fun isExistByUGId(userId: String, goodsId: String): Boolean

    @Query("SELECT COUNT(*) FROM history WHERE userId = :userId")
    suspend fun getCount(userId: String): Int
}
