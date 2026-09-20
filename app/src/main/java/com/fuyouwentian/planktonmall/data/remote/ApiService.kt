package com.fuyouwentian.planktonmall.data.remote

import com.fuyouwentian.planktonmall.data.model.HomeProduct
import com.fuyouwentian.planktonmall.data.model.HomeProductsRequest
import com.fuyouwentian.planktonmall.data.model.Product
import com.fuyouwentian.planktonmall.data.model.ProductsData
import com.fuyouwentian.planktonmall.data.model.ProductsRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("goods/banner")
    suspend fun getHomeBanner(): BaseResponse<ProductsData<HomeProduct>>

    @POST("goods/home")
    suspend fun getHomeProducts(@Body request: HomeProductsRequest): BaseResponse<ProductsData<HomeProduct>>

    @POST("goods/list")
    suspend fun getProducts(@Body request: ProductsRequest): BaseResponse<ProductsData<HomeProduct>>

    @GET("goods/{id}")
    suspend fun getProduct(@Path("id") id: String): BaseResponse<Product>
}