package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.PaginationParamsRequest
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.ProductsRequest
import com.fuyouwentian.planktonmall.domain.model.ProductsResponseData
import com.fuyouwentian.planktonmall.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService, // Hilt 会自动提供
) : ProductRepository {
    override suspend fun getProducts(request: ProductsRequest): ProductsResponseData<ProductLite>? {
        // 🌟 直接用 safeApiCall 包起来，它会返回 ProductsResponseData，或者抛出异常
        return safeApiCall { apiService.getProducts(request) }
    }

    override suspend fun getHomeCarousel(): List<ProductLite>? {
        return safeApiCall { apiService.getHomeCarousel() }
    }

    override suspend fun getRecommendProducts(request: PaginationParamsRequest?): ProductsResponseData<ProductLite>? {
        // 若传了 null，则给一个默认空实例
        val actualRequest = request ?: PaginationParamsRequest()
        return safeApiCall { apiService.getRecommendProducts(actualRequest) }
    }

    override suspend fun getProductDetail(id: String): Product? {
        return safeApiCall { apiService.getProductDetail(id) }
    }
}
