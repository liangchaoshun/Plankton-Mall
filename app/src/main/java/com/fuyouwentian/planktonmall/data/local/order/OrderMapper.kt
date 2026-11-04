package com.fuyouwentian.planktonmall.data.local.order

import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.domain.model.OrderRecord

fun OrderEntity.toDomain(): OrderRecord = OrderRecord(
    id = id,
    userId = userId,
    goodsId = goodsId,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsIcon = goodsIcon,
    goodsPrice = goodsPrice,
    quantity = quantity,
    actualPay = actualPay,
    receiverName = receiverName,
    receiverPhone = receiverPhone,
    receiverAddress = receiverAddress,
    orderStatus = orderStatus,
    paymentStatus = paymentStatus,
    afterSalesStatus = afterSalesStatus,
    paymentTime = paymentTime,
    deleted = deleted,
    createTime = createTime,
    updateTime = updateTime,
    syncState = syncState,
)

fun OrderRecord.toEntity(): OrderEntity = OrderEntity(
    id = id,
    userId = userId,
    goodsId = goodsId,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsIcon = goodsIcon,
    goodsPrice = goodsPrice,
    quantity = quantity,
    actualPay = actualPay,
    receiverName = receiverName,
    receiverPhone = receiverPhone,
    receiverAddress = receiverAddress,
    orderStatus = orderStatus,
    paymentStatus = paymentStatus,
    afterSalesStatus = afterSalesStatus,
    paymentTime = paymentTime,
    deleted = deleted,
    createTime = createTime,
    updateTime = updateTime,
    syncState = syncState ?: SyncRecordState.SYNCED,
)