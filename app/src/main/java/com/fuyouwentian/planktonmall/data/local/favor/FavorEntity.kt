package com.fuyouwentian.planktonmall.data.local.favor

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fuyouwentian.planktonmall.data.local.SyncRecordState

/**
 * SQLite 会为这两个字段建立 B+ 树索引，查询速度会极快
 * unique = true 能从数据库层面杜绝同一个用户重复收藏同一个商品，防止并发插入导致脏数据。
 * 只要唯一索引一样，即使 id 不同，就会自动替换掉旧记录，确保了本地永远只有一条
 */
@Entity(
    tableName = "favor",
    indices = [
        Index(value = ["userId", "goodsId"], unique = true)
    ]
)
data class FavorEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val goodsId: String,
    val isValid: Boolean,
    val goodsNameZh: String,
    val goodsNameEn: String,
    val goodsIcon: String,
    val goodsPrice: String,
    val goodsStock: Int,
    val goodsOnSale: Int,
    val createTime: String? = null,
    val updateTime: String? = null,
    val syncState: SyncRecordState? = SyncRecordState.SYNCED,
)
