package com.fuyouwentian.planktonmall.domain.model

import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MakeOrderRequest(
    @SerialName("_id")
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("goods_id")
    val goodsId: String,
    val quantity: Int,
    @SerialName("receiver_name")
    val receiverName: String,
    @SerialName("receiver_phone")
    val receiverPhone: String,
    @SerialName("receiver_address")
    val receiverAddress: String,
)

@Serializable
data class OrderRecord(
    @SerialName("_id")
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("goods_id")
    val goodsId: String,
    @SerialName("goods_name_zh")
    val goodsNameZh: String,
    @SerialName("goods_name_en")
    val goodsNameEn: String,
    @SerialName("goods_icon")
    val goodsIcon: String,
    @SerialName("goods_price")
    val goodsPrice: String,
    val quantity: Int,
    @SerialName("actual_pay")
    val actualPay: String,
    @SerialName("receiver_name")
    val receiverName: String,
    @SerialName("receiver_phone")
    val receiverPhone: String,
    @SerialName("receiver_address")
    val receiverAddress: String,
    @SerialName("order_status")
    val orderStatus: Int,
    @SerialName("payment_status")
    val paymentStatus: Int,
    @SerialName("after_sales_status")
    val afterSalesStatus: Int,
    @SerialName("payment_time")
    val paymentTime: String,
    val deleted: Int,
    @SerialName("create_time")
    val createTime: String? = null,
    @SerialName("update_time")
    val updateTime: String? = null,
    val syncState: SyncRecordState? = null
)
