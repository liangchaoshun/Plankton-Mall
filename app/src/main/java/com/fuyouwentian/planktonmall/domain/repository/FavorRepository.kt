package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.PaginationParameter
import com.fuyouwentian.planktonmall.domain.model.PaginationWithTotal
import kotlinx.coroutines.flow.Flow

interface FavorRepository {
    fun getRecordsFlow(userId: String?): Flow<List<FavorRecord>>
    suspend fun add2Favorites(request: FavorRecord): Boolean
    suspend fun deleteRecordByIndices(userId: String, goodsId: String): Boolean
    suspend fun getCount(userId: String): Int
    suspend fun isExist(userId: String, goodsId: String): Boolean
    suspend fun getRemoteRecords(params: PaginationParameter = PaginationParameter()): PaginationWithTotal
}
