package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.data.local.favor.FavorDao
import com.fuyouwentian.planktonmall.data.local.favor.toDomain
import com.fuyouwentian.planktonmall.data.local.favor.toEntity
import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.Add2FavorRequest
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.PaginationParameter
import com.fuyouwentian.planktonmall.domain.model.PaginationWithTotal
import com.fuyouwentian.planktonmall.domain.repository.FavorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class FavorRepositoryImpl @Inject constructor(
    private val apiService: ApiService, // Hilt 会自动提供
    private val favorDao: FavorDao,
    private val profileDao: ProfileDao,
) : FavorRepository {
    override fun getItemsFlowByUserId(userId: String?): Flow<List<FavorRecord>> {
        return favorDao.getItemsFlowByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun add2Favorites(request: FavorRecord): Boolean {
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
                return true
            }
        } catch (e: Exception) {
            favorDao.deleteItemByIndices(
                userId = request.userId,
                goodsId = request.goodsId
            ) // 同步到服务器失败，还原
            throw e
        }
        return false
    }

    override suspend fun deleteItemByIndices(userId: String, goodsId: String): Boolean {
        val isExist = favorDao.isExistByIndices(userId = userId, goodsId = goodsId)
        if (isExist) {
            val legacyEntity = favorDao.getItemByIndices(userId = userId, goodsId = goodsId)
            try {
                // 先处理本地数据
                favorDao.deleteItemByIndices(userId = userId, goodsId = goodsId)
                // 再同步到服务器
                safeApiCall { apiService.favorDelete(listOf(legacyEntity!!.id)) }?.let { return true }
            } catch (e: Exception) {
                favorDao.insert(legacyEntity!!) // 同步到服务器失败，还原
                throw e
            }
        }
        return false
    }

    override suspend fun getCountByUserId(userId: String): Int {
        return favorDao.getCountByUserId(userId)
    }

    override suspend fun isExist(userId: String, goodsId: String): Boolean {
        return favorDao.isExistByIndices(userId, goodsId)
    }

    override suspend fun getRemoteFavors(params: PaginationParameter): PaginationWithTotal {
        val response = safeApiCall { apiService.favorList(params) }
        response?.let {
            val list =
                it.data.map { item -> item.toEntity().copy(syncState = SyncRecordState.SYNCED) }
            // 清理旧数据
            if (list.isEmpty() || it.pageIndex == 1) {
                val userId = profileDao.getUserProfile()!!.id
                favorDao.deleteItemsByUserId(userId)
            }
            favorDao.insertMulti(list)
            delay(200.milliseconds) // 让 favorDao getItemsFlowByUserId 先发送信号
            return PaginationWithTotal(
                total = it.total,
                pageIndex = it.pageIndex,
                pageSize = it.pageSize
            )
        }
        return getRemoteFavors()
    }
}
