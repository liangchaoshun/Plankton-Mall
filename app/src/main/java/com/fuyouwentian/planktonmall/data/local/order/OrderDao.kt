package com.fuyouwentian.planktonmall.data.local.order

import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

interface OrderDao {
    // userId 为 null 时，
    // SQL 查不到数据，Room 会将这个空结果集转换为一个空的 List（即 emptyList()）
    @Query("SELECT * FROM orders WHERE userId = :userId")
    fun getRecordsFlow(userId: String?): Flow<List<OrderEntity>>

    @Upsert
    suspend fun upsert(item: OrderEntity)

    // 若是复合键，则判断复合键
    @Upsert
    suspend fun upsertMulti(items: List<OrderEntity>)

    // 清空 order 数据表，无论是谁的数据（慎用）
    @Query("DELETE FROM orders")
    suspend fun clearAll()

    // 即使没有 LIMIT，只要返回类型是单对象，空表会返回 null
    // 若返回 List<ClassName> 则为 emptyList()
    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE id IN (:ids) ")
    suspend fun getRecordsById(ids: List<String>): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE userId = :userId")
    suspend fun getRecordsByUser(userId: String): List<OrderEntity>

    // 通过 id 删除多条记录
    @Query("DELETE FROM orders WHERE id IN (:ids)")
    suspend fun deleteRecordsById(ids: List<String>)

    // 删除某用户的所有记录
    @Query("DELETE FROM orders WHERE userId = :userId")
    suspend fun deleteRecordsByUser(userId: String)

    // 返回 Boolean，只要存在就返回 true
    @Query("SELECT EXISTS(SELECT 1 FROM orders WHERE id = :id)")
    suspend fun isExistById(id: String): Boolean

    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId")
    suspend fun getCount(userId: String): Int
}
