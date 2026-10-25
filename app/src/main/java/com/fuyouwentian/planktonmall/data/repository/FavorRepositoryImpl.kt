package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.favor.FavorDao
import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
import com.fuyouwentian.planktonmall.domain.repository.FavorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavorRepositoryImpl @Inject constructor(
    private val favorDao: FavorDao,
    private val profileDao: ProfileDao,
) : FavorRepository {
    //
}