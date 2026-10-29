package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.PaginationParameter
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductDetailRequest
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.PaginationWithQuery
import com.fuyouwentian.planktonmall.domain.model.ProductsResponseData

interface ProductRepository {
    // 返回类型，List: 一次性异步获取、单次网络请求，Flow: 持续的异步数据流、监听数据库变化
    suspend fun getProducts(request: PaginationWithQuery): ProductsResponseData<ProductLite>?
    suspend fun getHomeCarousel(): List<ProductLite>?
    suspend fun getRecommendProducts(request: PaginationParameter? = null): ProductsResponseData<ProductLite>?
    suspend fun getProductDetail(param: ProductDetailRequest): Product?
}
