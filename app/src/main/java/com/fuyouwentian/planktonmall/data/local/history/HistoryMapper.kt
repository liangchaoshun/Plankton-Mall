package com.fuyouwentian.planktonmall.data.local.history

import com.fuyouwentian.planktonmall.domain.model.HistoryRecord

fun HistoryEntity.toDomain(): HistoryRecord = HistoryRecord(
    id = id,
    userId = userId,
    goodsId = goodsId,
    isValid = isValid,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsPrice = goodsPrice,
    goodsIcon = goodsIcon,
    goodsStock = goodsStock,
    goodsOnSale = goodsOnSale,
    createTime = createTime,
    updateTime = updateTime,
)

fun HistoryRecord.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    userId = userId,
    goodsId = goodsId,
    isValid = isValid,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsPrice = goodsPrice,
    goodsIcon = goodsIcon,
    goodsStock = goodsStock,
    goodsOnSale = goodsOnSale,
    createTime = createTime,
    updateTime = updateTime,
)
