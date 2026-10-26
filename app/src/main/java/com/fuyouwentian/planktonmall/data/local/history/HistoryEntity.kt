package com.fuyouwentian.planktonmall.data.local.history

import androidx.room.Entity

@Entity(
    tableName = "history",
    primaryKeys = ["createDate", "goodsId"] // 复合主键：日期 + 商品ID，前后顺序有区别
)
data class HistoryEntity(
    val userId: String,
    val goodsId: String,
    val isValid: Boolean,
    val goodsNameZh: String,
    val goodsNameEn: String,
    val goodsPrice: String,
    val goodsIcon: String,
    val goodsStock: Int,
    val goodsOnSale: Int,
    // 固定格式 "yyyy-MM-dd"，月和日必须是两位数，否则按日期排序不正确
    val createDate: String,
    val createTimeMillis: Long
)
