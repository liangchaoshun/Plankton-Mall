package com.fuyouwentian.planktonmall.data.local.favor

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fuyouwentian.planktonmall.data.local.SyncRecordState

@Entity(tableName = "favor")
data class FavorEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val goodsId: String,
    val isValid: Boolean,
    val goodsNameZh: String,
    val goodsNameEn: String,
    val goodsIcon: String,
    val goodsPrice: String,
    val goodsStock: Int,
    val goodsOnSale: Int,
    val createTime: String? = null,
    val updateTime: String? = null,
    val syncState: SyncRecordState? = SyncRecordState.SYNCED,
)
