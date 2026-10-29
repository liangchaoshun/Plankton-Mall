package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.PaginationParameter
import com.fuyouwentian.planktonmall.domain.model.PaginationWithTotal
import kotlinx.coroutines.flow.Flow

interface FavorRepository {
    fun getAllLocalFavorItems(): Flow<List<FavorRecord>>
    suspend fun getItemCountByGoodsId(userId: String, goodsId: String): Int
    suspend fun add2Favorites(request: FavorRecord): Boolean
    suspend fun removeFromFavorites(userId: String, goodsId: String): Boolean
    suspend fun getRemoteFavors(params: PaginationParameter = PaginationParameter()): PaginationWithTotal?
}