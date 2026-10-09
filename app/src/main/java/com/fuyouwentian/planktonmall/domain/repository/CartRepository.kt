package com.fuyouwentian.planktonmall.domain.repository;

import com.fuyouwentian.planktonmall.domain.model.CartProduct

interface CartRepository {
    // 返回类型，List: 一次性异步获取、单次网络请求，Flow: 持续的异步数据流、监听数据库变化
    suspend fun insert(param: CartProduct)
    suspend fun delete(ids: List<String>)
    suspend fun update(param: CartProduct)
    suspend fun fetchData(): List<CartProduct>
}
