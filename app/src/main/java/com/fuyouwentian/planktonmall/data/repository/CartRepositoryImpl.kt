package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.data.local.cart.CartDao
import com.fuyouwentian.planktonmall.data.local.cart.toDomain
import com.fuyouwentian.planktonmall.data.local.cart.toEntity
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

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val api: ApiService, // Hilt 会自动提供
    private val cartDao: CartDao,
) : CartRepository {
    // Room 框架在底层实现时，对于返回 Flow<List<T>> 的查询，
    // 如果数据库表中没有任何数据，它发射（emit）的是一个空的列表 emptyList()，而不是 null
    override fun getRecordsFlow(userId: String?): Flow<List<CartProduct>> {
        return cartDao.getRecordsFlow(userId).map { entities ->
            // 如果为空，entities 就是 emptyList，直接 map 返回空列表即可
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalQuantityFlow(userId: String): Flow<Int?> {
        return cartDao.getTotalQuantityFlow(userId)
    }

    override suspend fun syncLocalUpdate2Server(userId: String?) {
        val pendingItems = cartDao.getPendingUpdateRecords(userId).map { it.toDomain() }
        if (pendingItems.isNotEmpty()) {
            safeApiCall { api.cartUpdate(pendingItems) }?.let { bool ->
                if (bool) {
                    // 成功后，修改同步状态为“已同步”
                    cartDao.markRecords2Synced(pendingItems.map { it.id })
                }
            }
        }
    }

    // 参数是商品的参数
    override suspend fun upsert(userId: String, params: Add2CartProductParams): Boolean {
        // Log.d("CartRepositoryImpl ", "insert params: $params")
        val mongoIdStr = MongoIdGenerator.generate()
        var add2CartRequest = Add2CartRequest(
            id = mongoIdStr,
            goodsId = params.id
        )

        // 先处理本地数据
        val existingEntity = cartDao.getRecordByProductId(userId = userId, productId = params.id)
        if (existingEntity != null) {
            // 存在，更新本地数据
            val updatedEntity = existingEntity.copy(
                quantity = existingEntity.quantity + 1,
                checked = true, // 勾选上
                syncState = SyncRecordState.PENDING_UPDATE
            )
            cartDao.upsert(updatedEntity)
            add2CartRequest = Add2CartRequest(
                id = updatedEntity.id,
                goodsId = updatedEntity.goodsId
            )
        } else {
            // 不存在，插入一条新的到本地数据
            val cartProduct = CartProduct(
                id = mongoIdStr,
                userId = userId,
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
                syncState = SyncRecordState.PENDING_INSERT
            )
            cartDao.upsert(cartProduct.toEntity())
        }
        // 再同步到服务器
        var response = false
        try {
            safeApiCall { api.cartInsert(add2CartRequest) }?.let {
                cartDao.upsert(it.toEntity().copy(syncState = SyncRecordState.SYNCED))
                response = true
            }
        } catch (e: Exception) {
            cartDao.deleteRecordsByIds(listOf(mongoIdStr)) // 同步到服务器失败，还原
            throw e
        }
        return response
    }

    override suspend fun delete(ids: List<String>): Boolean {
        // 先删除本地数据
        val legacyItems = cartDao.getRecordsByIds(ids)
        cartDao.deleteRecordsByIds(ids)
        // 再同步到服务器
        try {
            safeApiCall { api.cartDelete(ids) }?.let { return it }
        } catch (e: Exception) {
            cartDao.upsertMulti(legacyItems) // 同步到服务器失败，还原
            throw e
        }
        return false
    }

    override suspend fun updateQuantity(id: String, quantity: Int) {
        cartDao.updateQuantity(id, quantity)
        cartDao.updateMultiChecked(listOf(id), true)
    }

    override suspend fun updateMultiChecked(ids: List<String>, checked: Boolean) {
        cartDao.updateMultiChecked(ids, checked)
    }

    override suspend fun fetchData() {
        val remoteData = safeApiCall { api.cartList() }
        remoteData?.let { if (remoteData.total > 0) cartDao.syncRemoteData2Room(remoteData.data) }
    }
}
