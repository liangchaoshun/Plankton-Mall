package com.fuyouwentian.planktonmall.data.local

enum class SyncRecordState {
    SYNCED, // 已与服务端一致
    PENDING_INSERT, // 本地已添加，待同步
    PENDING_UPDATE, // 本地已修改，待同步
}
