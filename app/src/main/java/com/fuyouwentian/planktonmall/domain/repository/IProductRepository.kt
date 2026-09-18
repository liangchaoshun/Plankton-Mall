package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.data.model.Product
import com.fuyouwentian.planktonmall.data.remote.ProductsData

interface IProductRepository {
    // 返回类型，List: 一次性异步获取、单次网络请求，Flow: 持续的异步数据流、监听数据库变化
    suspend fun getProducts(page: Int = 1, pageSize: Int = 20): ProductsData
    suspend fun getProduct(id: String): Product?
}
