package com.fuyouwentian.planktonmall.data.remote

import com.fuyouwentian.planktonmall.data.model.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("goods/list")
    suspend fun getProducts(@Body request: ProductsRequest): ProductsResponse

    @GET("goods/{id}")
    suspend fun getProduct(@Path("id") id: String): Product
}

data class ProductsRequest(
    val page_index: Int = 1,
    val page_size: Int = 20,
    val q: String? = ""
    // 其他后端要求的字段
)

data class ProductsResponse(
    val code: Int,
    val message: String,
    val data: List<Product>,   // 具体字段名以实际返回为准
)
