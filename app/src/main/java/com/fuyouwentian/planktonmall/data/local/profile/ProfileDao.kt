package com.fuyouwentian.planktonmall.data.local.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProfileDao {
    // 即使没有 LIMIT，只要返回类型是单对象（如 *Entity），空表依然会返回 null
    // 若返回 List<ClassName> 则为 emptyList()
    @Query("SELECT * FROM profile WHERE id = :id")
    suspend fun getProfile(id: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(item: ProfileEntity)

    // 清空整张数据表（慎用）
    @Query("DELETE FROM profile")
    suspend fun clearProfile()

    // 删除某个用户的信息
    @Query("DELETE FROM profile WHERE id = :id")
    suspend fun deleteProfile(id: String)
}
