package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.HomeProductsRequest
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductsResponseData
import com.fuyouwentian.planktonmall.domain.model.ProductsRequest

interface ProductRepository {
    // 返回类型，List: 一次性异步获取、单次网络请求，Flow: 持续的异步数据流、监听数据库变化
    suspend fun getProducts(request: ProductsRequest): ProductsResponseData<ProductLite>?
    suspend fun getHomeCarousel(): List<ProductLite>?
    suspend fun getHomeProducts(request: HomeProductsRequest): ProductsResponseData<ProductLite>?
    suspend fun getProductDetail(id: String): Product?
}
