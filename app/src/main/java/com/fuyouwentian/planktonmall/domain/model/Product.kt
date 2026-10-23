package com.fuyouwentian.planktonmall.domain.model

import com.fuyouwentian.planktonmall.data.local.cart.SyncState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Kotlin 序列化，默认会忽略掉数据类中没有的 JSON 字段
@Serializable
data class Product(
    @SerialName("_id")
    val id: String,
    @SerialName("name_zh")
    val nameZh: String,
    @SerialName("name_en")
    val nameEn: String,
    val price: String,
    @SerialName("home_banner")
    val homeBanner: Boolean,
    @SerialName("home_display")
    val homeDisplay: Boolean,
    @SerialName("icon_url")
    val iconUrl: String,
    @SerialName("banner_video_url")
    val bannerVideoUrl: String,
    @SerialName("banner_model_url")
    val bannerModelUrl: String,
    @SerialName("series_id")
    val seriesId: String,
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("on_sale")
    val onSale: Int,
    val stock: Int,
    val deleted: Int,
    @SerialName("desc_url")
    val descUrl: List<String>,
    @SerialName("banner_url")
    val bannerUrl: List<String>,
    @SerialName("create_time")
    val createTime: String,
    @SerialName("update_time")
    val updateTime: String
) {
    // 计算属性：是否已删除
    val isDeleted: Boolean get() = deleted == 1

    // 计算属性：是否已下架 (0 代表下架, 1 表示在售)
    val isOffShelf: Boolean get() = onSale == 0

    // 综合判断：是否为无效商品（已删除 或 已下架）
    val isInvalid: Boolean get() = isDeleted || isOffShelf
}

@Serializable
data class ProductLite(
    @SerialName("_id")
    val id: String,
    @SerialName("name_zh")
    val nameZh: String,
    @SerialName("name_en")
    val nameEn: String,
    val price: String,
    val cover: String,
    @SerialName("icon_url")
    val iconUrl: String,
    @SerialName("series_id")
    val seriesId: String,
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("on_sale")
    val onSale: Int,
    val stock: Int
)

@Serializable
data class CartProduct(
    @SerialName("_id")
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("goods_id")
    val goodsId: String,
    @SerialName("goods_price")
    val goodsPrice: String,
    val quantity: Int,
    @SerialName("is_valid")
    val isValid: Boolean,
    val checked: Boolean,
    @SerialName("goods_stock")
    val goodsStock: Int,
    @SerialName("goods_on_sale")
    val goodsOnSale: Int,
    @SerialName("goods_name_zh")
    val goodsNameZh: String,
    @SerialName("goods_name_en")
    val goodsNameEn: String,
    @SerialName("goods_icon")
    val goodsIcon: String,
    @SerialName("create_time")
    val createTime: String? = null,
    @SerialName("update_time")
    val updateTime: String? = null,
    @SerialName("sync_state")
    val syncState: SyncState? = null
)

data class Add2CartProductParams(
    val id: String,
    val price: String,
    val stock: Int,
    val onSale: Int,
    val nameZh: String,
    val nameEn: String,
    val iconUrl: String,
)

@Serializable
data class Add2CartRequest(
    val id: String,
    @SerialName("goods_id")
    val goodsId: String,
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
data class PaginationParamsRequest(
    @SerialName("page_index")
    val pageIndex: Int = 1,
    @SerialName("page_size")
    val pageSize: Int = 10,
    // 其他后端要求的字段
)


@Serializable
data class ProductsResponseData<T>(
    val data: List<T>,
    val total: Int,
    @SerialName("page_index")
    val pageIndex: Int,
    @SerialName("page_size")
    val pageSize: Int
)

@Serializable
data class CartProductsResponseData<T>(
    val data: List<T>,
    val total: Int
)
