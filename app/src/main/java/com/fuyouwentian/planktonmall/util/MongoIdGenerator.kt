package com.fuyouwentian.planktonmall.util

import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicInteger

/**
 * 1. MongoDB 的 ObjectId 组成结构：
 *  4 字节：Unix 时间戳（秒级）；
 *  5 字节：随机值（机器/进程标识）；
 *  3 字节：自增计数器，确保同一台设备、同一秒内连续生成的多个 ID 不会重复，支持单进程每秒 1677 万个唯一 ID；
 * 2. 计算机中，1 个字节（8 bits） 恰好可以用 2 个十六进制字符来表示（4 个 bit 用一个 16 进制字符表示：2^4 = 16）
 *  例如：字节 0110 0001（十进制 97，字符 'a'），转换成十六进制是 61
 */
object MongoIdGenerator {
    private val random = SecureRandom()
    private val counter = AtomicInteger(random.nextInt())
    private val machineId = ByteArray(5).apply { random.nextBytes(this) }

    fun generate(): String {
        // 4 字节时间戳
        val timestamp = (System.currentTimeMillis() / 1000).toInt()
        val timestampBytes = ByteArray(4)
        timestampBytes[0] = (timestamp shr 24).toByte()
        timestampBytes[1] = (timestamp shr 16).toByte()
        timestampBytes[2] = (timestamp shr 8).toByte()
        timestampBytes[3] = timestamp.toByte()

        // 3 字节自增计数器
        val count = counter.getAndIncrement()
        val counterBytes = byteArrayOf(
            (count shr 16).toByte(),
            (count shr 8).toByte(),
            count.toByte()
        )

        // 合并字节：4(时间) + 5(机器) + 3(计数)
        val allBytes = timestampBytes + machineId + counterBytes

        // 转成 16 进制字符串，得到长度为 24 的字符串
        return allBytes.joinToString("") { "%02x".format(it) }
    }
}