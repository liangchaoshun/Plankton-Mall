package com.fuyouwentian.planktonmall.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.fuyouwentian.planktonmall.data.local.PrefsKeys
import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.data.local.favor.FavorDao
import com.fuyouwentian.planktonmall.data.local.favor.toDomain
import com.fuyouwentian.planktonmall.data.local.favor.toEntity
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.Add2FavorRequest
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.PaginationParameter
import com.fuyouwentian.planktonmall.domain.model.PaginationWithTotal
import com.fuyouwentian.planktonmall.domain.repository.FavorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class FavorRepositoryImpl @Inject constructor(
    private val apiService: ApiService, // Hilt 会自动提供
    private val favorDao: FavorDao,
    private val dataStore: DataStore<Preferences>
) : FavorRepository {
    override fun getRecordsFlow(userId: String?): Flow<List<FavorRecord>> {
        return favorDao.getRecordsFlow(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun add2Favorites(request: FavorRecord): Boolean {
        try {
            // 先处理本地数据
            favorDao.upsert(request.toEntity())
            // 再同步到服务器
            safeApiCall {
                apiService.favorInsert(
                    Add2FavorRequest(
                        id = request.id,
                        goodsId = request.goodsId
                    )
                )
            }?.let {
                favorDao.upsert(it.toEntity().copy(syncState = SyncRecordState.SYNCED))
                return true
            }
        } catch (e: Exception) {
            favorDao.deleteRecordByIndices(
                userId = request.userId,
                goodsId = request.goodsId
            ) // 同步到服务器失败，还原
            throw e
        }
        return false
    }

    override suspend fun deleteRecordByIndices(userId: String, goodsId: String): Boolean {
        val isExist = favorDao.isExistByIndices(userId = userId, goodsId = goodsId)
        if (isExist) {
            val legacyEntity = favorDao.getRecordByIndices(userId = userId, goodsId = goodsId)
            try {
                // 先处理本地数据
                favorDao.deleteRecordByIndices(userId = userId, goodsId = goodsId)
                // 再同步到服务器
                safeApiCall { apiService.favorDelete(listOf(legacyEntity!!.id)) }?.let { return true }
            } catch (e: Exception) {
                favorDao.upsert(legacyEntity!!) // 同步到服务器失败，还原
                throw e
            }
        }
        return false
    }

    override suspend fun getCount(userId: String): Int {
        return favorDao.getCount(userId)
    }

    override suspend fun isExist(userId: String, goodsId: String): Boolean {
        return favorDao.isExistByIndices(userId, goodsId)
    }

    override suspend fun getRemoteRecords(params: PaginationParameter): PaginationWithTotal {
        val response = safeApiCall { apiService.favorList(params) }
        response?.let {
            val list =
                it.data.map { item -> item.toEntity().copy(syncState = SyncRecordState.SYNCED) }
            if (list.isEmpty() && it.pageIndex == 1) {
                val userId = dataStore.data
                    .catch { e ->
                        // DataStore 读取时若发生 IO 异常（如文件损坏），则 emit 一个 emptyPreferences() 给下游，避免崩溃
                        if (e is IOException) emit(emptyPreferences()) else throw e
                    }
                    .first()[PrefsKeys.USER_ID]
                userId?.let { favorDao.deleteRecordsByUser(userId) }
            }
            favorDao.upsertMulti(list)
            delay(200.milliseconds) // 让 favorDao getRecordsFlow 先发送信号
            return PaginationWithTotal(
                total = it.total,
                pageIndex = it.pageIndex,
                pageSize = it.pageSize
            )
        }
        return PaginationWithTotal()
    }
}
