package com.fuyouwentian.planktonmall.data.remote

import com.fuyouwentian.planktonmall.data.model.Product
import com.fuyouwentian.planktonmall.data.model.ProductsData
import com.fuyouwentian.planktonmall.data.model.ProductsRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("goods/list")
    suspend fun getProducts(@Body request: ProductsRequest): BaseResponse<ProductsData>

    @GET("goods/{id}")
    suspend fun getProduct(@Path("id") id: String): BaseResponse<Product>
}