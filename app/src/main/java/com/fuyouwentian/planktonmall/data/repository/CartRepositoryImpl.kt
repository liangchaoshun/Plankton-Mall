package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.cart.CartDao
import com.fuyouwentian.planktonmall.data.local.cart.toEntity
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val api: ApiService, // Hilt 会自动提供
    private val cartDao: CartDao,
) : CartRepository {
    override suspend fun insert(param: CartProduct) {
        safeApiCall { api.cartInsert(param) }
        cartDao.insert(param.toEntity()) // 本地同步
    }

    override suspend fun delete(ids: List<String>) {
        safeApiCall { api.cartDelete(ids) }
        cartDao.deleteByIds(ids)
    }

    override suspend fun update(param: CartProduct) {
        safeApiCall { api.cartUpdate(param) }
        cartDao.update(param.toEntity())
    }

    override suspend fun fetchData(): List<CartProduct> {
        // 网络优先：成功则写回到 Room 并返回
        val remote = safeApiCall { api.cartList() }
        cartDao.clearAll()
        cartDao.insertAll(remote.map { it.toEntity() })
        return remote
    }
}
