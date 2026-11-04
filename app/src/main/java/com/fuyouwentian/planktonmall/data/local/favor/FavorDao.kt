package com.fuyouwentian.planktonmall.data.local.favor

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavorDao {
    // userId 为 null 时，
    // SQL 查不到数据，Room 会将这个空结果集转换为一个空的 List（即 emptyList()）
    @Query("SELECT * FROM favor WHERE userId = :userId")
    fun getRecordsFlow(userId: String?): Flow<List<FavorEntity>>

    @Upsert
    suspend fun upsert(item: FavorEntity)

    // 若是复合键，则判断复合键
    @Upsert
    suspend fun upsertMulti(items: List<FavorEntity>)

    // 清空 favor 数据表，无论是谁的数据（慎用）
    @Query("DELETE FROM favor")
    suspend fun clearAll()

    // 删除某用户的所有记录
    @Query("DELETE FROM favor WHERE userId = :userId")
    suspend fun deleteRecordsByUser(userId: String)

    // 通过索引删除一条记录
    @Query("DELETE FROM favor WHERE userId = :userId AND goodsId = :goodsId")
    suspend fun deleteRecordByIndices(userId: String, goodsId: String)

    // 通过 id 删除多条记录
    @Query("DELETE FROM favor WHERE id IN (:ids)")
    suspend fun deleteRecordsById(ids: List<String>)

    // 即使没有 LIMIT，只要返回类型是单对象，空表会返回 null
    // 若返回 List<ClassName> 则为 emptyList()
    @Query("SELECT * FROM favor WHERE userId = :userId AND goodsId = :goodsId LIMIT 1")
    suspend fun getRecordByIndices(userId: String, goodsId: String): FavorEntity?

    // 返回 Boolean，只要存在就返回 true
    @Query("SELECT EXISTS(SELECT 1 FROM favor WHERE userId = :userId AND goodsId = :goodsId)")
    suspend fun isExistByIndices(userId: String, goodsId: String): Boolean

    @Query("SELECT COUNT(*) FROM favor WHERE userId = :userId")
    suspend fun getCount(userId: String): Int
}
