package com.weng.maphw.di

import com.weng.maphw.model.repository.GeoRepository
import com.weng.maphw.model.repository.GeoRepositoryImpl
import org.koin.dsl.module

val userModule = module {
    single<GeoRepository> { GeoRepositoryImpl(get()) }
}