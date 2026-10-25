package com.fuyouwentian.planktonmall.data.local.history

import com.fuyouwentian.planktonmall.domain.model.HistoryProduct

fun HistoryEntity.toDomain(): HistoryProduct = HistoryProduct(
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

fun HistoryProduct.toEntity(): HistoryEntity = HistoryEntity(
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
