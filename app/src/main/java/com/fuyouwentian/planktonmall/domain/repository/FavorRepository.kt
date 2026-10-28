package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.FavorsPaginationState
import com.fuyouwentian.planktonmall.domain.model.PaginationRequest
import kotlinx.coroutines.flow.Flow

interface FavorRepository {
    fun getAllLocalFavorItems(): Flow<List<FavorRecord>>
    suspend fun getItemCountByGoodsId(goodsId: String): Int
    suspend fun add2Favorites(request: FavorRecord): Boolean
    suspend fun removeFromFavorites(goodsId: String): Boolean
    suspend fun getRemoteFavors(params: PaginationRequest = PaginationRequest()): FavorsPaginationState?
}