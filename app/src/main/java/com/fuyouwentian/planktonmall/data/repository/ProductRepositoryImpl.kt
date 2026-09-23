package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.model.ProductLite
import com.fuyouwentian.planktonmall.data.model.HomeProductsRequest
import com.fuyouwentian.planktonmall.data.model.Product
import com.fuyouwentian.planktonmall.data.model.ProductsData
import com.fuyouwentian.planktonmall.data.model.ProductsRequest
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.repository.IProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService, // Hilt 会自动提供
) : IProductRepository {
    override suspend fun getProducts(request: ProductsRequest): ProductsData<ProductLite> {
        // 🌟 直接用 safeApiCall 包起来，它会返回 ProductsData，或者抛出异常
        return safeApiCall { apiService.getProducts(request) }
    }

    override suspend fun getHomeBanner(): ProductsData<ProductLite> {
        return safeApiCall { apiService.getHomeBanner() }
    }

    override suspend fun getHomeProducts(request: HomeProductsRequest): ProductsData<ProductLite> {
        return safeApiCall { apiService.getHomeProducts(request) }
    }

    override suspend fun getProduct(id: String): Product? = try {
        safeApiCall { apiService.getProduct(id) }
    } catch (e: retrofit2.HttpException) {
        if (e.code() == 404) null else throw e
    }
}
