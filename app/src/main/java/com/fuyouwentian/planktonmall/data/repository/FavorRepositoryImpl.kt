package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.data.local.favor.FavorDao
import com.fuyouwentian.planktonmall.data.local.favor.toDomain
import com.fuyouwentian.planktonmall.data.local.favor.toEntity
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.Add2FavorRequest
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.FavorsPaginationState
import com.fuyouwentian.planktonmall.domain.model.PaginationRequest
import com.fuyouwentian.planktonmall.domain.repository.FavorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavorRepositoryImpl @Inject constructor(
    private val apiService: ApiService, // Hilt 会自动提供
    private val favorDao: FavorDao
) : FavorRepository {
    override fun getAllLocalFavorItems(): Flow<List<FavorRecord>> {
        return favorDao.getAllItems().map { entities ->
            // 如果为空，entities 就是 emptyList，直接 map 返回空列表即可
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getItemCountByGoodsId(goodsId: String): Int {
        return favorDao.getItemCountByGoodsId(goodsId)
    }

    override suspend fun add2Favorites(request: FavorRecord): Boolean {
        var resultBool = false
        try {
            // 先处理本地数据
            favorDao.insert(request.toEntity())
            // 再同步到服务器
            safeApiCall {
                apiService.favorInsert(
                    Add2FavorRequest(
                        id = request.id,
                        goodsId = request.goodsId
                    )
                )
            }?.let {
                favorDao.insert(it.toEntity().copy(syncState = SyncRecordState.SYNCED))
                resultBool = true
            }
        } catch (e: Exception) {
            favorDao.deleteItemsByIds(listOf(request.id)) // 同步到服务器失败，还原
            throw e
        }
        return resultBool
    }

    override suspend fun removeFromFavorites(goodsId: String): Boolean {
        var resultBool = false
        val existingEntity = favorDao.getItemByGoodsId(goodsId)
        existingEntity?.let {
            val ids = listOf(existingEntity.id)
            try {
                // 先处理本地数据
                favorDao.deleteItemsByIds(ids)
                // 再同步到服务器
                safeApiCall { apiService.favorDelete(ids) }?.let { resultBool = true }
            } catch (e: Exception) {
                favorDao.insert(existingEntity) // 同步到服务器失败，还原
                throw e
            }
        }
        return resultBool
    }

    override suspend fun getRemoteFavors(params: PaginationRequest): FavorsPaginationState? {
        val response = safeApiCall { apiService.favorList(params) }
        response?.let {
            val list =
                it.data.map { item -> item.toEntity().copy(syncState = SyncRecordState.SYNCED) }
            favorDao.insertMulti(list)
            return FavorsPaginationState(
                total = it.total,
                pageIndex = it.pageIndex,
                pageSize = it.pageSize
            )
        }
        return null
    }
}