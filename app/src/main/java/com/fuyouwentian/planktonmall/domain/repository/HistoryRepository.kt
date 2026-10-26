package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.HistoryRecord
import com.fuyouwentian.planktonmall.domain.model.Product

interface HistoryRepository {
    suspend fun insert(product: Product)
    suspend fun delete(ids: List<String>)
    suspend fun getAllItems(): List<HistoryRecord>
}
