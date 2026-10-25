package com.fuyouwentian.planktonmall.data.local.favor

import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.domain.model.FavorProduct
import kotlin.Int

fun FavorEntity.toDomain(): FavorProduct = FavorProduct(
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
    syncState = syncState,
)

fun FavorProduct.toEntity(): FavorEntity = FavorEntity(
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
    syncState = syncState ?: SyncRecordState.SYNCED,
)