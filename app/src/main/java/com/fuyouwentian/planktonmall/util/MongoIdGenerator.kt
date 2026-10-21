package com.fuyouwentian.planktonmall.util

import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicInteger

/**
 * MongoDB 的 ObjectId 结构是：
 * 4 字节：Unix 时间戳（秒级）
 * 5 字节：随机值（机器/进程标识）
 * 3 字节：自增计数器
 */
object MongoIdGenerator {
    private val random = SecureRandom()
    private val counter = AtomicInteger(random.nextInt())
    private val machineId = ByteArray(5).apply { random.nextBytes(this) }

    fun generate(): String {
        // 1. 4 字节时间戳
        val timestamp = (System.currentTimeMillis() / 1000).toInt()
        val timestampBytes = ByteArray(4)
        timestampBytes[0] = (timestamp shr 24).toByte()
        timestampBytes[1] = (timestamp shr 16).toByte()
        timestampBytes[2] = (timestamp shr 8).toByte()
        timestampBytes[3] = timestamp.toByte()

        // 2. 3 字节自增计数器
        val count = counter.getAndIncrement()
        val counterBytes = byteArrayOf(
            (count shr 16).toByte(),
            (count shr 8).toByte(),
            count.toByte()
        )

        // 合并字节：4(时间) + 5(机器) + 3(计数)
        val allBytes = timestampBytes + machineId + counterBytes

        // 转成 16 进制字符串
        return allBytes.joinToString("") { "%02x".format(it) }
    }
}