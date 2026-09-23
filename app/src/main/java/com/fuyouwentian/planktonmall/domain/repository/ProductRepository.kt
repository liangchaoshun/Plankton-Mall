package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.HomeProductsRequest
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductsData
import com.fuyouwentian.planktonmall.domain.model.ProductsRequest

interface ProductRepository {
    // 返回类型，List: 一次性异步获取、单次网络请求，Flow: 持续的异步数据流、监听数据库变化
    suspend fun getProducts(request: ProductsRequest): ProductsData<ProductLite>
    suspend fun getHomeBanner(): ProductsData<ProductLite>
    suspend fun getHomeProducts(request: HomeProductsRequest): ProductsData<ProductLite>
    suspend fun getProduct(id: String): Product?
}
