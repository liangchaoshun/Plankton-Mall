package com.fuyouwentian.planktonmall.domain.model

import androidx.room.TypeConverter
import com.fuyouwentian.planktonmall.common.GlobalConfig
import kotlinx.serialization.builtins.ListSerializer

class ReceiverInfoConverter {
    // 将 List 转换为 String 存入数据库
    @TypeConverter
    fun convertReceiverInfo2String(list: List<ReceiverInfo>?): String {
        if (list == null) return "[]"
        return GlobalConfig.json.encodeToString(
            ListSerializer(ReceiverInfo.serializer()),
            list
        )
    }

    // 将 String 从数据库读出为 List
    @TypeConverter
    fun convertReceiverInfo2List(jsonString: String?): List<ReceiverInfo> {
        if (jsonString.isNullOrBlank()) return emptyList()
        return try {
            GlobalConfig.json.decodeFromString(
                ListSerializer(ReceiverInfo.serializer()),
                jsonString
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // 解析失败时兜底返回空列表
        }
    }
}