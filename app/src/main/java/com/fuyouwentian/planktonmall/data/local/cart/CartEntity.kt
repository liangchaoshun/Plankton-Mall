package com.fuyouwentian.planktonmall.data.local.cart

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val goodsId: String,
    val isValid: Boolean,
    val checked: Boolean,
    val quantity: Int,
    val price: String,
    val goodsOnSale: Int,
    val goodsStock: Int,
    val goodsNameZh: String,
    val goodsNameEn: String,
    val goodsIcon: String,
    val syncState: SyncState = SyncState.SYNCED,
)

enum class SyncState {
    SYNCED,          // 已与服务端一致
    PENDING_UPDATE,  // 本地已修改，待同步
    PENDING_DELETE   // 本地已删除，待同步
}
