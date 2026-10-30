package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.PaginationParameter
import com.fuyouwentian.planktonmall.domain.model.PaginationWithTotal
import kotlinx.coroutines.flow.Flow

interface FavorRepository {
    fun getItemsFlowByUserId(userId: String?): Flow<List<FavorRecord>>
    suspend fun add2Favorites(request: FavorRecord): Boolean
    suspend fun deleteItemByIndices(userId: String, goodsId: String): Boolean
    suspend fun getCountByUserId(userId: String): Int
    suspend fun isExist(userId: String, goodsId: String): Boolean
    suspend fun getRemoteFavors(params: PaginationParameter = PaginationParameter()): PaginationWithTotal
}
