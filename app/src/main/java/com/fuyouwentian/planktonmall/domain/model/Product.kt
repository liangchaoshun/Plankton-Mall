package com.fuyouwentian.planktonmall.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Kotlin 序列化，默认会忽略掉数据类中没有的 JSON 字段
@Serializable
data class Product(
    @SerialName("_id")
    val id: String,
    val name_zh: String,
    val name_en: String,
    val price: String,
    val home_banner: Boolean,
    val home_display: Boolean,
    val icon_url: String,
    val banner_video_url: String,
    val banner_model_url: String,
    val series_id: String,
    val category_id: String,
    val desc_url: List<String>,
    val banner_url: List<String>,
    val create_time: String,
    val update_time: String
)

@Serializable
data class ProductLite(
    @SerialName("_id")
    val id: String,
    val name_zh: String,
    val name_en: String,
    val price: String,
    val cover: String,
    val icon_url: String,
    val series_id: String,
    val category_id: String
)

@Serializable
data class CartProduct(
    @SerialName("_id")
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("goods_id")
    val goodsId: String,
    @SerialName("is_valid")
    val isValid: Boolean,
    val checked: Boolean,
    val quantity: Int,
    val price: String,
    @SerialName("goods_name_zh")
    val goodsNameZh: String,
    @SerialName("goods_name_en")
    val goodsNameEn: String,
    @SerialName("goods_icon")
    val goodsIcon: String,
)

@Serializable
data class ProductsRequest(
    @SerialName("page_index")
    val pageIndex: Int = 1,
    @SerialName("page_size")
    val pageSize: Int = 10,
    val q: String? = ""
    // 其他后端要求的字段
)

@Serializable
data class HomeProductsRequest(
    @SerialName("page_index")
    val pageIndex: Int = 1,
    @SerialName("page_size")
    val pageSize: Int = 10,
    // 其他后端要求的字段
)


@Serializable
data class ProductsData<T>(
    val data: List<T>,
    val total: Int,
    @SerialName("page_index")
    val pageIndex: Int,
    @SerialName("page_size")
    val pageSize: Int
)
