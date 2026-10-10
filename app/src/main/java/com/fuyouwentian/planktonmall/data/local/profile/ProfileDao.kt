package com.fuyouwentian.planktonmall.data.local.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = :id LIMIT 1")
    suspend fun getUserProfile(id: String): Flow<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(item: ProfileEntity)

    @Query("DELETE FROM profile") // SQLite 语法没有 DELETE *
    suspend fun clearUserProfile()
}
