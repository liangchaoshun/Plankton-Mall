package com.fuyouwentian.planktonmall.data.local.cart

import com.fuyouwentian.planktonmall.domain.model.CartProduct

/**
 * Mapper 映射函数
 * 即使 CartProduct 使用了 @SerialName 注解映射字段，还需要映射函数
 * 因为 CartProduct 和 CartEntity 仍是两个不同的类，Kotlin 不会自动互转
 */

// Entity -> Domain
fun CartEntity.toDomain(): CartProduct = CartProduct(
    id = id,
    userId = userId,
    goodsId = goodsId,
    isValid = isValid,
    checked = checked,
    quantity = quantity,
    price = price,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsIcon = goodsIcon,
)

// Domain -> Entity
fun CartProduct.toEntity(): CartEntity = CartEntity(
    id = id,
    userId = userId,
    goodsId = goodsId,
    isValid = isValid,
    checked = checked,
    quantity = quantity,
    price = price,
    goodsNameZh = goodsNameZh,
    goodsNameEn = goodsNameEn,
    goodsIcon = goodsIcon,
)