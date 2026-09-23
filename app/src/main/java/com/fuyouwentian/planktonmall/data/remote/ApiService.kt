package com.fuyouwentian.planktonmall.data.remote

import com.fuyouwentian.planktonmall.domain.model.Category
import com.fuyouwentian.planktonmall.domain.model.CategoryData
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.HomeProductsRequest
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductsData
import com.fuyouwentian.planktonmall.domain.model.ProductsRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("goods/banner")
    suspend fun getHomeBanner(): BaseResponse<ProductsData<ProductLite>>

    @POST("goods/home")
    suspend fun getHomeProducts(@Body request: HomeProductsRequest): BaseResponse<ProductsData<ProductLite>>

    @POST("goods/list")
    suspend fun getProducts(@Body request: ProductsRequest): BaseResponse<ProductsData<ProductLite>>

    @GET("goods/{id}")
    suspend fun getProduct(@Path("id") id: String): BaseResponse<Product>

    @GET("category/list")
    suspend fun getCategories(): BaseResponse<CategoryData<Category>>
}
