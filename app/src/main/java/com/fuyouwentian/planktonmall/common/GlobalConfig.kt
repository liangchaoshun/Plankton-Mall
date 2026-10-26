package com.fuyouwentian.planktonmall.common

import kotlinx.serialization.json.Json

object GlobalConfig {
    // Json 实例
    val json = Json {
        ignoreUnknownKeys = true // 核心：遇到不认识的字段不报错，直接忽略
        coerceInputValues = true // 可选：如果遇到 null 但非空字段，尝试给默认值
        encodeDefaults = true // 即使是默认值也转换
    }
}