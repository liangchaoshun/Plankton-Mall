package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.cart.CartDao
import com.fuyouwentian.planktonmall.data.local.cart.SyncState
import com.fuyouwentian.planktonmall.data.local.cart.toDomain
import com.fuyouwentian.planktonmall.data.local.cart.toEntity
import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
import com.fuyouwentian.planktonmall.data.local.profile.toDomain
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.Add2CartProductParams
import com.fuyouwentian.planktonmall.domain.model.Add2CartRequest
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
    override suspend fun insert(params: Add2CartProductParams): Boolean {
        // Log.d("CartRepositoryImpl ", "insert params: $params")
        var add2CartRequest = Add2CartRequest(
            id = MongoIdGenerator.generate(),
            goodsId = params.id
        )

        // 先处理本地数据
        val existingEntity = cartDao.getItemByProductId(params.id)
        if (existingEntity != null) {
            // 存在，更新本地数据
            val updatedEntity = existingEntity.copy(
                quantity = existingEntity.quantity + 1,
                checked = true, // 勾选上
                syncState = SyncState.PENDING_UPDATE
            )
            cartDao.update(updatedEntity)
            add2CartRequest = Add2CartRequest(
                id = updatedEntity.id,
                goodsId = updatedEntity.goodsId
            )
        } else {
            // 不存在，插入一条新的到本地数据
            val cartProduct = CartProduct(
                id = add2CartRequest.id,
                userId = profileDao.getUserProfile()!!.toDomain().id,
                goodsId = params.id,
                goodsPrice = params.price,
                quantity = 1,
                isValid = true,
                checked = true,
                goodsStock = params.stock,
                goodsOnSale = params.onSale,
                goodsNameZh = params.nameZh,
                goodsNameEn = params.nameEn,
                goodsIcon = params.iconUrl,
                syncState = SyncState.PENDING_UPDATE
            )
            cartDao.insert(cartProduct.toEntity())
        }
        // 再同步到服务器
        var response = false
        try {
            safeApiCall { api.cartInsert(add2CartRequest) }?.let {
                cartDao.update(it.toEntity().copy(syncState = SyncState.SYNCED))
                response = true
            }
        } catch (e: Exception) {
            cartDao.deleteItemsByIds(listOf(add2CartRequest.id)) // 同步到服务器失败，还原
            throw e
        }
        return response
    }

    override suspend fun delete(ids: List<String>): Boolean {
        // 先删除本地数据
        val legacyItems = cartDao.getItemsByIds(ids)
        cartDao.deleteItemsByIds(ids)
        // 再同步到服务器
        var response = false
        try {
            safeApiCall { api.cartDelete(ids) }?.let { response = it }
        } catch (e: Exception) {
            cartDao.insertMulti(legacyItems) // 同步到服务器失败，还原
            throw e
        }
        return response
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
