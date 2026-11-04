package com.fuyouwentian.planktonmall.domain.repository;

import com.fuyouwentian.planktonmall.domain.model.OrderRecord
import com.fuyouwentian.planktonmall.domain.model.PaginationWithQuery
import com.fuyouwentian.planktonmall.domain.model.PaginationWithTotal
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getRecordsFlow(userId: String?): Flow<List<OrderRecord>>
    suspend fun makeOrder(params: List<OrderRecord>): Boolean
    suspend fun deleteRecordsByUser(userId: String): Boolean
    suspend fun deleteRecordsById(ids: List<String>): Boolean
    suspend fun getCount(userId: String): Int
    suspend fun isExist(id: String): Boolean
    suspend fun getRemoteRecords(params: PaginationWithQuery = PaginationWithQuery()): PaginationWithTotal
}
