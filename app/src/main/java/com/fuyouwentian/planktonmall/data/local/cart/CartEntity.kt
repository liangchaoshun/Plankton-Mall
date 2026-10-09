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
    val goodsNameZh: String,
    val goodsNameEn: String,
    val goodsIcon: String
)