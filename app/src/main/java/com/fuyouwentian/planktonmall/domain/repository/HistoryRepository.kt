package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.data.local.history.HistoryEntity
import com.fuyouwentian.planktonmall.domain.model.HistoryRecord

interface HistoryRepository {
    suspend fun upsert(record: HistoryRecord)
    suspend fun upsertMulti(items: List<HistoryEntity>)
    suspend fun getItemsByUserId(id: String): List<HistoryRecord>
    suspend fun deleteItemsByUserId(userId: String)
    suspend fun deleteItemByIndices(userId: String, goodsId: String, createDate: String)
    suspend fun deleteItemsByDate(userId: String, createDate: String)
    suspend fun isExistByUGId(userId: String, goodsId: String): Boolean
    suspend fun getCountByUserId(userId: String): Int
}
