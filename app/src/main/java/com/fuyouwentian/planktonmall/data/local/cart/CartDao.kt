package com.fuyouwentian.planktonmall.data.local.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    /**
     * 1. getAllItemFlow 不能加 suspend 关键字，仅负责“订阅”，没有真正的数据库读取操作，
     * 2. 真正的读取是异步的，通过 .stateIn() 或 collect 开始收集数据时，
     *  2.1 Room 内部的协程机制会自动把底层的 SQLite 查询、数据监听（通过 InvalidationTracker）放到后台线程去执行；
     *  2.2 一旦数据发生变化，Room 会在后台查出数据，然后再通过 Flow 发射出来；
     * 3. Room 的官方设计：在 Room 中，返回 Flow、LiveData 或 RxJava 的查询方法，天生就是“响应式且异步”的，不需要也不应该标记为 suspend。
     */
    @Query("SELECT * FROM cart")
    fun getAllItemFlow(): Flow<List<CartEntity>>

    @Query("SELECT id FROM cart")
    // Room 官方原生也支持直接返回 Set，底层处理转换
    fun getAllItemIds(): List<String>

    @Query("SELECT * FROM cart WHERE goodsId = :productId")
    suspend fun getItemByProductId(productId: String): CartEntity?

    @Query("SELECT * FROM cart WHERE id IN (:ids)")
    suspend fun getItemsByIds(ids: List<String>): List<CartEntity>

    // REPLACE 策略，若主键相同（同一个商品），则会覆盖旧数据
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMulti(items: List<CartEntity>)

    @Update
    suspend fun update(item: CartEntity)

    /*@Query("DELETE FROM cart WHERE id = :id")
    suspend fun deleteById(id: String)
    */
    @Query("DELETE FROM cart WHERE id IN (:ids)")
    suspend fun deleteItemsByIds(ids: List<String>)

    @Query("UPDATE cart SET syncState = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markLocalItems2Synced(ids: List<String>)

    @Query("UPDATE cart SET quantity = :quantity, syncState = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int)

    @Query("UPDATE cart SET checked = :checked, syncState = 'PENDING_UPDATE' WHERE id IN (:ids)")
    suspend fun updateMultiChecked(ids: List<String>, checked: Boolean)

    // 查询所有待同步数据
    @Query("SELECT * FROM cart WHERE syncState != 'SYNCED'")
    suspend fun getAllPendingSyncedItems(): List<CartEntity>

    @Transaction
    suspend fun syncRemoteData2Room(remote: List<CartProduct>) {
        val remoteIds = remote.map { it.id }
        val pendingMap = getItemsByIds(remoteIds).associateBy { it.id }

        // 先转成 Entity，再将 pending 中的字段：checked、quantity、syncState，覆盖 remote 中相同字段
        val mergedEntities = remote
            .map { item ->
                val remoteEntity = item.toEntity()
                val localItem = pendingMap[item.id]
                if (localItem != null) {
                    remoteEntity.copy(
                        checked = localItem.checked,
                        quantity = localItem.quantity,
                        syncState = localItem.syncState, // 让同步接口，处理待同步的数据
                    )
                } else {
                    remoteEntity
                }
            }
        // 统一插入/更新
        insertMulti(mergedEntities)
        // 删除掉多余的脏数据
        val allLocalCartIds = getAllItemIds()
        val dirtyIds = allLocalCartIds.filter { !remoteIds.contains(it) }
        if (dirtyIds.isNotEmpty()) deleteItemsByIds(dirtyIds)
    }
}
