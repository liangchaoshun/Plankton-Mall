package com.fuyouwentian.planktonmall.data.local.cart

import androidx.room.Entity
import androidx.room.PrimaryKey

// 关于 syncState 字段的类型，
// 在 Room 中，如果要保存枚举类型，
// 默认行为是将枚举的 name 属性（即常量名，如 "PENDING_UPDATE"）作为 String 存入数据库。
@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val goodsId: String,
    val goodsPrice: String,
    val quantity: Int,
    val isValid: Boolean,
    val checked: Boolean,
    val goodsStock: Int,
    val goodsOnSale: Int,
    val goodsNameZh: String,
    val goodsNameEn: String,
    val goodsIcon: String,
    val createTime: String? = null,
    val updateTime: String? = null,
    val syncState: SyncState? = SyncState.SYNCED,
)

enum class SyncState {
    SYNCED, // 已与服务端一致
    PENDING_UPDATE, // 本地已修改，待同步
}
