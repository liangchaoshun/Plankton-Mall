package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.Add2CartProductParams
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    // 返回类型，List: 一次性异步获取、单次网络请求，Flow: 持续的异步数据流、监听数据库变化
    fun getRecordsFlow(userId: String?): Flow<List<CartProduct>>
    fun getTotalQuantityFlow(userId: String): Flow<Int?>
    suspend fun syncLocalUpdate2Server(userId: String?)
    suspend fun upsert(userId: String, params: Add2CartProductParams): Boolean
    suspend fun delete(ids: List<String>): Boolean
    suspend fun fetchData()

    // 会将多条记录设置为同一个值
    suspend fun updateMultiChecked(ids: List<String>, checked: Boolean)
    suspend fun updateQuantity(id: String, quantity: Int)
}
