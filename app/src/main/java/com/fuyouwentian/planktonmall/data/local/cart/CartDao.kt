package com.fuyouwentian.planktonmall.data.local.cart

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    /**
     * 1. getRecordsFlow 不能加 suspend 关键字，仅负责“订阅”，没有真正的数据库读取操作，
     * 2. 真正的读取是异步的，通过 .stateIn() 或 collect 开始收集数据时，
     *  2.1 Room 内部的协程机制会自动把底层的 SQLite 查询、数据监听（通过 InvalidationTracker）放到后台线程去执行；
     *  2.2 一旦数据发生变化，Room 会在后台查出数据，然后再通过 Flow 发射出来；
     * 3. Room 的官方设计：在 Room 中，返回 Flow、LiveData 或 RxJava 的查询方法，天生就是“响应式且异步”的，不需要也不应该标记为 suspend。
     * 4. userId 为 null 时，SQL 查不到数据，Room 会将这个空结果集转换为一个空的 List（即 emptyList()）
     */
    @Query("SELECT * FROM cart WHERE userId = :userId")
    fun getRecordsFlow(userId: String?): Flow<List<CartEntity>>

    // 购物车为空时，SUM 会返回 NULL
    @Query("SELECT SUM(quantity) FROM cart WHERE userId = :userId")
    fun getTotalQuantityFlow(userId: String): Flow<Int?>

    @Query("SELECT id FROM cart WHERE userId = :userId")
    // Room 官方原生也支持直接返回 Set，底层自动处理转换
    fun getRecordsId(userId: String): List<String>

    @Query("SELECT * FROM cart WHERE id IN (:ids)")
    suspend fun getRecordsByIds(ids: List<String>): List<CartEntity>

    @Query("SELECT * FROM cart WHERE userId = :userId AND goodsId = :productId")
    suspend fun getRecordByProductId(userId: String, productId: String): CartEntity?

    @Upsert
    suspend fun upsert(record: CartEntity)

    @Upsert
    suspend fun upsertMulti(records: List<CartEntity>)

    @Query("DELETE FROM cart WHERE id IN (:ids)")
    suspend fun deleteRecordsByIds(ids: List<String>)

    // 删除某用户的所有记录
    @Query("DELETE FROM cart WHERE userId = :userId")
    suspend fun deleteRecordsByUser(userId: String)

    @Query("UPDATE cart SET syncState = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markRecords2Synced(ids: List<String>)

    @Query("UPDATE cart SET quantity = :quantity, syncState = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int)

    @Query("UPDATE cart SET checked = :checked, syncState = 'PENDING_UPDATE' WHERE id IN (:ids)")
    suspend fun updateMultiChecked(ids: List<String>, checked: Boolean)

    // 查询所有待更新的数据，“待插入”和“待删除”的数据是另外处理
    @Query("SELECT * FROM cart WHERE userId = :userId AND syncState = 'PENDING_UPDATE'")
    suspend fun getPendingUpdateRecords(userId: String?): List<CartEntity>

    @Transaction
    suspend fun syncRemoteData2Room(remote: List<CartProduct>) {
        val remoteIds = remote.map { it.id }
        val pendingMap = getRecordsByIds(remoteIds).associateBy { it.id }

        // 先转成 Entity，再将 pending 中的字段：checked、quantity、syncState，覆盖 remote 中相同字段
        val mergedList = remote
            .map { record ->
                val remoteEntity = record.toEntity()
                val localRecord = pendingMap[record.id]
                if (localRecord != null) {
                    remoteEntity.copy(
                        checked = localRecord.checked,
                        quantity = localRecord.quantity,
                        syncState = localRecord.syncState, // 让同步接口，处理待同步的数据
                    )
                } else {
                    remoteEntity
                }
            }
        // 统一插入/更新
        upsertMulti(mergedList)
        // 删除掉多余的脏数据
        val dirtyIds = getRecordsId(mergedList.first().userId).filter { !remoteIds.contains(it) }
        if (dirtyIds.isNotEmpty()) deleteRecordsByIds(dirtyIds)
    }
}
