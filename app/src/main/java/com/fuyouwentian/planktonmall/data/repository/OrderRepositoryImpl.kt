package com.fuyouwentian.planktonmall.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.fuyouwentian.planktonmall.data.local.PrefsKeys
import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.data.local.order.OrderDao
import com.fuyouwentian.planktonmall.data.local.order.OrderEntity
import com.fuyouwentian.planktonmall.data.local.order.toDomain
import com.fuyouwentian.planktonmall.data.local.order.toEntity
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.MakeOrderRequest
import com.fuyouwentian.planktonmall.domain.model.OrderRecord
import com.fuyouwentian.planktonmall.domain.model.PaginationWithQuery
import com.fuyouwentian.planktonmall.domain.model.PaginationWithTotal
import com.fuyouwentian.planktonmall.domain.repository.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class OrderRepositoryImpl @Inject constructor(
    private val apiService: ApiService, // Hilt 会自动提供
    private val orderDao: OrderDao,
    private val dataStore: DataStore<Preferences>
) : OrderRepository {
    override fun getRecordsFlow(userId: String?): Flow<List<OrderRecord>> {
        return orderDao.getRecordsFlow(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun makeOrder(params: List<OrderRecord>): Boolean {
        try {
            val localList = mutableListOf<OrderEntity>()
            val requestList = mutableListOf<MakeOrderRequest>()

            params.forEach {
                localList.add(it.toEntity())
                requestList.add(
                    MakeOrderRequest(
                        id = it.id,
                        goodsId = it.goodsId,
                        quantity = it.quantity,
                        receiverName = it.receiverName,
                        receiverPhone = it.receiverPhone,
                        receiverAddress = it.receiverAddress,
                    )
                )
            }
            // 先处理本地数据
            orderDao.upsertMulti(localList)
            // 再同步到服务器
            safeApiCall {
                apiService.orderInsert(requestList)
            }?.let { list ->
                val records = list.map {
                    it.toEntity().copy(syncState = SyncRecordState.SYNCED)
                }
                orderDao.upsertMulti(records)
                return true
            }
        } catch (e: Exception) {
            orderDao.deleteRecordsById(params.map { it.id }) // 同步到服务器失败，还原
            throw e
        }
        return false
    }

    // 删除某个账号下的所有记录
    override suspend fun deleteRecordsByUser(userId: String): Boolean {
        val legacyEntities = orderDao.getRecordsByUser(userId = userId)
        try {
            // 先处理本地数据
            orderDao.deleteRecordsByUser(userId = userId)
            // 再同步到服务器
            safeApiCall { apiService.orderClear() }?.let { return true }
        } catch (e: Exception) {
            orderDao.upsertMulti(legacyEntities) // 同步到服务器失败，还原
            throw e
        }
        return false
    }

    // 通过 id 列表删除多条记录
    override suspend fun deleteRecordsById(ids: List<String>): Boolean {
        val legacyEntities = orderDao.getRecordsById(ids)
        try {
            // 先处理本地数据
            orderDao.deleteRecordsById(ids)
            // 再同步到服务器
            safeApiCall { apiService.orderDelete(ids) }?.let { return true }
        } catch (e: Exception) {
            orderDao.upsertMulti(legacyEntities) // 同步到服务器失败，还原
            throw e
        }
        return false
    }


    override suspend fun getCount(userId: String): Int {
        return orderDao.getCount(userId)
    }

    override suspend fun isExist(id: String): Boolean {
        return orderDao.isExistById(id)
    }

    override suspend fun getRemoteRecords(params: PaginationWithQuery): PaginationWithTotal {
        val response = safeApiCall { apiService.orderList(params) }
        response?.let {
            val list =
                it.data.map { item -> item.toEntity().copy(syncState = SyncRecordState.SYNCED) }
            // TODO 清理旧数据
            Log.d("[getRemoteRecords]", "list: $list")
            if (list.isEmpty() || it.pageIndex == 1) {
                val userId = dataStore.data
                    .catch { e ->
                        // DataStore 读取时若发生 IO 异常（如文件损坏），则 emit 一个 emptyPreferences() 给下游，避免崩溃
                        if (e is IOException) emit(emptyPreferences()) else throw e
                    }
                    .first()[PrefsKeys.USER_ID]
                userId?.let { orderDao.deleteRecordsByUser(userId) }
            }
            orderDao.upsertMulti(list)
            delay(200.milliseconds) // 让 orderDao getRecordsFlow 先发送信号
            return PaginationWithTotal(
                total = it.total,
                pageIndex = it.pageIndex,
                pageSize = it.pageSize
            )
        }
        return PaginationWithTotal()
    }
}