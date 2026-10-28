package com.fuyouwentian.planktonmall.data.local.favor

import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.domain.model.FavorRecord

fun FavorEntity.toDomain(): FavorRecord = FavorRecord(
    id = id,
    userId = userId,
    goodsId = goodsId,
    isValid = isValid,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsIcon = goodsIcon,
    goodsPrice = goodsPrice,
    goodsStock = goodsStock,
    goodsOnSale = goodsOnSale,
    createTime = createTime,
    updateTime = updateTime,
    syncState = syncState,
)

fun FavorRecord.toEntity(): FavorEntity = FavorEntity(
    id = id,
    userId = userId,
    goodsId = goodsId,
    isValid = isValid,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsIcon = goodsIcon,
    goodsPrice = goodsPrice,
    goodsStock = goodsStock,
    goodsOnSale = goodsOnSale,
    createTime = createTime,
    updateTime = updateTime,
    syncState = syncState ?: SyncRecordState.SYNCED,
)