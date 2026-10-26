package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.HistoryRecord

interface HistoryRepository {
    suspend fun insert(record: HistoryRecord)
    suspend fun delete(ids: List<String>)
    suspend fun getAllItems(): List<HistoryRecord>
}
