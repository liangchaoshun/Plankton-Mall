package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.cart.CartDao
import com.fuyouwentian.planktonmall.data.local.cart.SyncState
import com.fuyouwentian.planktonmall.data.local.cart.toDomain
import com.fuyouwentian.planktonmall.data.local.cart.toEntity
import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
import com.fuyouwentian.planktonmall.data.local.profile.toDomain
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.Add2CartParams
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.util.MongoIdGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// 本地优先 + syncState 标记 + 后台同步
// 这样可以做到 UI 立即响应
@Singleton
class CartRepositoryImpl @Inject constructor(
    private val api: ApiService, // Hilt 会自动提供
    private val cartDao: CartDao,
    private val profileDao: ProfileDao,
) : CartRepository {

    override suspend fun syncLocal2Server() {
        val pendingItems = cartDao.getAllPendingSyncedItems().map { it.toDomain() }
//        Log.d("CartRepositoryImpl", "syncLocal2Server: $pendingItems")
        if (pendingItems.isNotEmpty()) {
            safeApiCall { api.cartSyncLocal2Server(pendingItems) }?.let { bool ->
                if (bool) {
                    // 成功后，修改同步状态
                    cartDao.markLocalItems2Synced(pendingItems.map { it.id })
                }
            }
        }
    }

    // Room 框架在底层实现时，对于返回 Flow<List<T>> 的查询，
    // 如果数据库表中没有任何数据，它发射（emit）的是一个空的列表 emptyList()，而不是 null
    override fun getAllLocalCartItems(): Flow<List<CartProduct>> {
        return cartDao.getAllItemFlow().map { entities ->
            // 如果为空，entities 就是 emptyList，直接 map 返回空列表即可
            entities.map { it.toDomain() }
        }
    }

    // 参数是商品的参数
    override suspend fun insert(params: Add2CartParams) {
        // Log.d("CartRepositoryImpl ", "insert params: $params")
        val existingEntity = cartDao.getItemByProductId(params.id)
        if (existingEntity != null) {
            // 已存在：数量 + 1，并标记为待更新
            val updatedEntity = existingEntity.copy(
                quantity = existingEntity.quantity + 1,
                checked = true, // 勾选上
                syncState = SyncState.PENDING_UPDATE
            )
            cartDao.update(updatedEntity)
        } else {
            // 不存在：直接插入新记录
            val userId = profileDao.getUserProfile()!!.toDomain().id
            val cartProduct = CartProduct(
                id = MongoIdGenerator.generate(),
                userId = userId,
                goodsId = params.id,
                goodsPrice = params.price,
                quantity = 1,
                isValid = true,
                checked = true, // 勾选上
                goodsStock = params.stock,
                goodsOnSale = params.onSale,
                goodsNameZh = params.nameZh,
                goodsNameEn = params.nameEn,
                goodsIcon = params.iconUrl,
                syncState = SyncState.PENDING_UPDATE
            )
            cartDao.insert(cartProduct.toEntity())
        }
        syncLocal2Server()
    }

    override suspend fun delete(ids: List<String>) {
        cartDao.markLocalItems2Deleted(ids)
        syncLocal2Server()
    }

    override suspend fun updateMultiChecked(ids: List<String>, checked: Boolean) {
        cartDao.updateMultiChecked(ids, checked)
        // 要做防抖，所以同步到服务器方法（syncLocal2Server）须在 CartViewModel 中调用
    }

    override suspend fun updateQuantity(id: String, quantity: Int) {
        cartDao.updateQuantity(id, quantity)
        cartDao.updateMultiChecked(listOf(id), true)
        // 要做防抖，所以同步到服务器方法（syncLocal2Server）须在 CartViewModel 中调用
    }

    override suspend fun fetchData() {
        val remoteData = safeApiCall { api.cartList() }
        remoteData?.let { if (remoteData.total > 0) cartDao.syncRemoteData2Room(remoteData.data) }
    }
}
