package com.fuyouwentian.planktonmall.data.local.favor

import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.domain.model.FavorProduct

fun FavorEntity.toDomain(): FavorProduct = FavorProduct(
    id = id,
    userId = userId,
    goodsId = goodsId,
    isValid = isValid,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsIcon = goodsIcon,
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
    goodsIcon = goodsIcon,
    createTime = createTime,
    updateTime = updateTime,
    syncState = syncState ?: SyncRecordState.SYNCED,
)