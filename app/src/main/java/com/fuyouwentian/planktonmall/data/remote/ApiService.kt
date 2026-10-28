package com.fuyouwentian.planktonmall.data.remote

import com.fuyouwentian.planktonmall.domain.model.Add2CartRequest
import com.fuyouwentian.planktonmall.domain.model.Add2FavorRequest
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import com.fuyouwentian.planktonmall.domain.model.CartProductsResponseData
import com.fuyouwentian.planktonmall.domain.model.Category
import com.fuyouwentian.planktonmall.domain.model.CategoryData
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.LoginRequest
import com.fuyouwentian.planktonmall.domain.model.LoginResponse
import com.fuyouwentian.planktonmall.domain.model.PaginationRequest
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductDetailRequest
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.ProductsRequest
import com.fuyouwentian.planktonmall.domain.model.ProductsResponseData
import com.fuyouwentian.planktonmall.domain.model.RegisterRequest
import com.fuyouwentian.planktonmall.domain.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("goods/carousel")
    suspend fun getHomeCarousel(): BaseResponse<List<ProductLite>?>

    @POST("goods/recommend")
    suspend fun getRecommendProducts(@Body param: PaginationRequest?): BaseResponse<ProductsResponseData<ProductLite>?>

    @POST("goods/list")
    suspend fun getProducts(@Body param: ProductsRequest): BaseResponse<ProductsResponseData<ProductLite>?>

    @POST("goods/detail")
    suspend fun getProductDetail(@Body param: ProductDetailRequest): BaseResponse<Product?>

    @GET("category/list")
    suspend fun getCategories(): BaseResponse<CategoryData<Category>?>

    @POST("auth/login")
    suspend fun userLogin(@Body param: LoginRequest): BaseResponse<LoginResponse?>

    @POST("auth/register")
    suspend fun userRegister(@Body param: RegisterRequest): BaseResponse<Boolean?>

    @GET("user/profile")
    suspend fun userGetProfile(): BaseResponse<UserProfile?>

    @GET("auth/logout")
    suspend fun userLogout(): BaseResponse<Boolean?>

    @GET("auth/validate")
    suspend fun userValidate(): BaseResponse<Boolean?>

    @POST("cart/insert")
    suspend fun cartInsert(@Body param: Add2CartRequest): BaseResponse<CartProduct>

    @POST("cart/delete")
    suspend fun cartDelete(@Body ids: List<String>): BaseResponse<Boolean>

    @PUT("cart/update")
    suspend fun cartUpdate(@Body params: List<CartProduct>): BaseResponse<Boolean?>

    @GET("cart/list")
    suspend fun cartList(): BaseResponse<CartProductsResponseData<CartProduct>?>

    @POST("favor/insert")
    suspend fun favorInsert(@Body param: Add2FavorRequest): BaseResponse<FavorRecord>

    @POST("favor/delete")
    suspend fun favorDelete(@Body ids: List<String>): BaseResponse<Boolean>

    @POST("favor/list")
    suspend fun favorList(@Body param: PaginationRequest): BaseResponse<ProductsResponseData<FavorRecord>?>

}
