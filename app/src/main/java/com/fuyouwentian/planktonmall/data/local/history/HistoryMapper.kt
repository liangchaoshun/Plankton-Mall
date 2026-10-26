package com.fuyouwentian.planktonmall.data.local.history

import com.fuyouwentian.planktonmall.domain.model.HistoryRecord

fun HistoryEntity.toDomain(): HistoryRecord = HistoryRecord(
    userId = userId,
    goodsId = goodsId,
    isValid = isValid,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsPrice = goodsPrice,
    goodsIcon = goodsIcon,
    goodsStock = goodsStock,
    goodsOnSale = goodsOnSale,
    createDate = createDate,
    createTimeMillis = createTimeMillis,
)

fun HistoryRecord.toEntity(): HistoryEntity = HistoryEntity(
    userId = userId,
    goodsId = goodsId,
    isValid = isValid,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsPrice = goodsPrice,
    goodsIcon = goodsIcon,
    goodsStock = goodsStock,
    goodsOnSale = goodsOnSale,
    createDate = createDate,
    createTimeMillis = createTimeMillis,
)
