package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.history.HistoryDao
import com.fuyouwentian.planktonmall.data.local.history.HistoryEntity
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
    override suspend fun upsert(record: HistoryRecord) {
        val list = historyDao.getItemsByGoodsIds(listOf(record.goodsId))
        // 更新相同商品（不同用户、日期）的一些字段
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
        // 将同一件商品，不同用户、日期的所有记录，都更新下某些字段。再添加进去
        val pendingList = pruneLegacyList.toMutableList().apply { add(record.toEntity()) }
        historyDao.upsertMulti(pendingList)
    }

    override suspend fun upsertMulti(items: List<HistoryEntity>) {
        historyDao.upsertMulti(items)
    }

    override suspend fun getItems(id: String): List<HistoryRecord> {
        return historyDao.getItems(id).map { it.toDomain() }
    }

    override suspend fun deleteItems(userId: String) {
        historyDao.deleteItems(userId)
    }

    override suspend fun deleteItemByIndices(
        userId: String,
        goodsId: String,
        createDate: String
    ) {
        historyDao.deleteItemByIndices(
            userId = userId,
            goodsId = goodsId,
            createDate = createDate
        )
    }

    override suspend fun deleteItemsByDate(userId: String, createDate: String) {
        historyDao.deleteItemsByDate(userId = userId, createDate = createDate)
    }

    override suspend fun isExistByUGId(
        userId: String,
        goodsId: String
    ): Boolean {
        return historyDao.isExistByUGId(userId, goodsId)
    }

    override suspend fun getCount(userId: String): Int {
        return historyDao.getCount(userId)
    }
}
