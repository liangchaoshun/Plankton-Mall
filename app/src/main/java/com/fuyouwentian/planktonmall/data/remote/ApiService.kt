package com.fuyouwentian.planktonmall.data.remote

import com.fuyouwentian.planktonmall.domain.model.CartProduct
import com.fuyouwentian.planktonmall.domain.model.Category
import com.fuyouwentian.planktonmall.domain.model.CategoryData
import com.fuyouwentian.planktonmall.domain.model.HomeProductsRequest
import com.fuyouwentian.planktonmall.domain.model.LoginRequest
import com.fuyouwentian.planktonmall.domain.model.LoginResponse
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.ProductsData
import com.fuyouwentian.planktonmall.domain.model.ProductsRequest
import com.fuyouwentian.planktonmall.domain.model.RegisterRequest
import com.fuyouwentian.planktonmall.domain.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("goods/carousel")
    suspend fun getHomeCarousel(): BaseResponse<List<ProductLite>>

    @POST("goods/home")
    suspend fun getHomeProducts(@Body request: HomeProductsRequest): BaseResponse<ProductsData<ProductLite>>

    @POST("goods/list")
    suspend fun getProducts(@Body request: ProductsRequest): BaseResponse<ProductsData<ProductLite>>

    @GET("goods/{id}")
    suspend fun getProduct(@Path("id") id: String): BaseResponse<Product>

    @GET("category/list")
    suspend fun getCategories(): BaseResponse<CategoryData<Category>>

    @POST("auth/login")
    suspend fun userLogin(param: LoginRequest): BaseResponse<LoginResponse>

    @GET("auth/user/{id}")
    suspend fun userGetProfile(@Path("id") id: String): BaseResponse<UserProfile>

    @GET("auth/logout")
    suspend fun userLogout(): BaseResponse<Unit>

    @POST("auth/register")
    suspend fun userRegister(param: RegisterRequest): BaseResponse<Unit>

    @POST("cart/insert")
    suspend fun cartInsert(param: CartProduct): BaseResponse<Unit>

    @DELETE("cart/delete")
    suspend fun cartDelete(ids: List<String>): BaseResponse<Unit>

    @PUT("cart/update")
    suspend fun cartUpdate(param: CartProduct): BaseResponse<Unit>

    @POST("cart/list")
    suspend fun cartList(): BaseResponse<List<CartProduct>>
}
