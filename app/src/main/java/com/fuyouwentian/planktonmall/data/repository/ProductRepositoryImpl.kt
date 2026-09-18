package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.model.Product
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.ProductsRequest
import com.fuyouwentian.planktonmall.domain.repository.IProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val api: ApiService, // Hilt 会自动提供
) : IProductRepository {
    override suspend fun getProducts(page: Int, pageSize: Int): List<Product> {
        val response = api.getProducts(ProductsRequest(page_index = page, page_size = pageSize))
        return response.data
    }

    override suspend fun getProduct(id: String): Product? = try {
        api.getProduct(id)
    } catch (e: retrofit2.HttpException) {
        if (e.code() == 404) null else throw e
    }
}
