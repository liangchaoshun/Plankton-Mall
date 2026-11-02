package com.fuyouwentian.planktonmall.di

import com.fuyouwentian.planktonmall.core.event.GlobalEventBus
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GlobalEventBusEntryPoint {
    fun getGlobalEventBus(): GlobalEventBus
}