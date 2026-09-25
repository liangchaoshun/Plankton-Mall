package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.HttpExceptionWrapper
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.HomeProductsRequest
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.ProductsData
import com.fuyouwentian.planktonmall.domain.model.ProductsRequest
import com.fuyouwentian.planktonmall.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService, // Hilt 会自动提供
) : ProductRepository {
    override suspend fun getProducts(request: ProductsRequest): ProductsData<ProductLite> {
        // 🌟 直接用 safeApiCall 包起来，它会返回 ProductsData，或者抛出异常
        return safeApiCall { apiService.getProducts(request) }
    }

    override suspend fun getHomeCarousel(): List<ProductLite> {
        return safeApiCall { apiService.getHomeCarousel() }
    }

    override suspend fun getHomeProducts(request: HomeProductsRequest): ProductsData<ProductLite> {
        return safeApiCall { apiService.getHomeProducts(request) }
    }

    override suspend fun getProduct(id: String): Product? = try {
        safeApiCall { apiService.getProduct(id) }
    } catch (e: HttpExceptionWrapper) {
        if (e.code == 404) null else throw e
    }
}
