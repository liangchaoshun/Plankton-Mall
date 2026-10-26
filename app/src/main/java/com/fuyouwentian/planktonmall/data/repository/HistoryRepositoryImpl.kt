package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.history.HistoryDao
import com.fuyouwentian.planktonmall.data.local.history.toDomain
import com.fuyouwentian.planktonmall.data.local.history.toEntity
import com.fuyouwentian.planktonmall.domain.model.HistoryRecord
import com.fuyouwentian.planktonmall.domain.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao,
) : HistoryRepository {
    override suspend fun insert(record: HistoryRecord) {
        historyDao.insert(record.toEntity())
    }

    override suspend fun delete(ids: List<String>) {
        historyDao.deleteItemsByIds(ids)
    }

    override suspend fun getAllItems(): List<HistoryRecord> {
        return historyDao.getAllItems().map { it.toDomain() }
    }
}
