package com.fuyouwentian.planktonmall.data.local.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val goodsId: String,
    val isValid: Boolean,
    val goodsNameZh: String,
    val goodsNameEn: String,
    val goodsPrice: String,
    val goodsIcon: String,
    val goodsStock: Int,
    val goodsOnSale: Int,
    val createTime: String? = null,
    val updateTime: String? = null,
)
