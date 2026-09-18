package com.fuyouwentian.planktonmall.data.remote

import com.fuyouwentian.planktonmall.data.model.Product
import kotlinx.serialization.Serializable
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

@Serializable
data class ProductsRequest(
    val page_index: Int = 1,
    val page_size: Int = 20,
    val q: String? = ""
    // 其他后端要求的字段
)

@Serializable
data class ProductsResponse(
    val status_code: String, // 注意：后端返回的是字符串 "20000"，所以这里用 String
    val data: ProductsData,  // 这里指向下面定义的嵌套类
    val message: String
)

@Serializable
data class ProductsData(
    val data: List<Product>,
    val total: Int,
    val page_index: Int,
    val page_size: Int
)