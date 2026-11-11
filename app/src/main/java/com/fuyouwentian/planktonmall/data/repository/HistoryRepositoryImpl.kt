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
        val list = historyDao.getRecordsByGoodsIds(listOf(record.goodsId))
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

    override suspend fun upsertMulti(records: List<HistoryEntity>) {
        historyDao.upsertMulti(records)
    }

    override suspend fun getRecords(id: String): List<HistoryRecord> {
        return historyDao.getRecords(id).map { it.toDomain() }
    }

    override suspend fun deleteRecordsByUser(userId: String) {
        historyDao.deleteRecordsByUser(userId)
    }

    override suspend fun deleteRecordByIndices(
        userId: String,
        goodsId: String,
        createDate: String
    ) {
        historyDao.deleteRecordByIndices(
            userId = userId,
            goodsId = goodsId,
            createDate = createDate
        )
    }

    override suspend fun deleteRecordsByDate(userId: String, createDate: String) {
        historyDao.deleteRecordsByDate(userId = userId, createDate = createDate)
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
