package com.fuyouwentian.planktonmall.data.local.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProfileDao {
    // 即使没有 LIMIT，只要返回类型是单对象（如 ProfileEntity），空表依然会返回 null
    // 若返回 List<ClassName> 则为 emptyList()
    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun getUserProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(item: ProfileEntity)

    @Query("DELETE FROM profile") // SQLite 语法没有 DELETE *
    suspend fun clearUserProfile()
}
