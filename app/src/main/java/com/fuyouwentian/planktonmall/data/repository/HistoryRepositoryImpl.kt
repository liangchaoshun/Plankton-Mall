package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.history.HistoryDao
import com.fuyouwentian.planktonmall.data.local.history.toDomain
import com.fuyouwentian.planktonmall.data.local.history.toEntity
import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
import com.fuyouwentian.planktonmall.data.local.profile.toDomain
import com.fuyouwentian.planktonmall.domain.model.HistoryProduct
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao,
    private val profileDao: ProfileDao,
) : HistoryRepository {
    override suspend fun insert(product: Product) {
        val historyProduct = HistoryProduct(
            id = product.id,
            userId = profileDao.getUserProfile()!!.toDomain().id,
            goodsId = product.id,
            isValid = product.deleted == 0,
            goodsNameZh = product.nameZh,
            goodsNameEn = product.nameEn,
            goodsPrice = product.price,
            goodsIcon = product.iconUrl,
            goodsStock = product.stock,
            goodsOnSale = product.onSale,
            createTime = product.createTime,
            updateTime = product.updateTime,
        )
        historyDao.insert(historyProduct.toEntity())
    }

    override suspend fun delete(ids: List<String>) {
        historyDao.deleteItemsByIds(ids)
    }

    override suspend fun getAllItems(): List<HistoryProduct> {
        return historyDao.getAllItems().map { it.toDomain() }
    }
}
