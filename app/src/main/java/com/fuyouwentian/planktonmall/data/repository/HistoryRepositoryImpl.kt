package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.history.HistoryDao
import com.fuyouwentian.planktonmall.data.local.history.toDomain
import com.fuyouwentian.planktonmall.data.local.history.toEntity
import com.fuyouwentian.planktonmall.domain.model.HistoryRecord
import com.fuyouwentian.planktonmall.domain.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao,
) : HistoryRepository {
    override suspend fun insert(record: HistoryRecord) {
        val list = historyDao.getItemsByIds(listOf(record.goodsId))
        // 更新相同商品（不同日期）的一些字段
        val pruneLegacyList = list.map {
            it.copy(
                isValid = record.isValid,
                goodsNameZh = record.goodsNameZh,
                goodsNameEn = record.goodsNameEn,
                goodsPrice = record.goodsPrice,
                goodsIcon = record.goodsIcon,
                goodsStock = record.goodsStock,
                goodsOnSale = record.goodsOnSale,
            )
        }
        val pendingList = pruneLegacyList.toMutableList().apply { add(record.toEntity()) }
        historyDao.insertMulti(pendingList)
    }

    override suspend fun delete(ids: List<String>) {
        historyDao.deleteItemsByIds(ids)
    }

    override suspend fun getAllItems(): List<HistoryRecord> {
        return historyDao.getAllItems().map { it.toDomain() }
    }
}
