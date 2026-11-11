package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.data.local.history.HistoryEntity
import com.fuyouwentian.planktonmall.domain.model.HistoryRecord

interface HistoryRepository {
    suspend fun upsert(record: HistoryRecord)
    suspend fun upsertMulti(records: List<HistoryEntity>)
    suspend fun getRecords(id: String): List<HistoryRecord>
    suspend fun deleteRecordsByUser(userId: String)
    suspend fun deleteRecordByIndices(userId: String, goodsId: String, createDate: String)
    suspend fun deleteRecordsByDate(userId: String, createDate: String)
    suspend fun isExistByUGId(userId: String, goodsId: String): Boolean
    suspend fun getCount(userId: String): Int
}
