package com.weng.maphw.di

import com.weng.maphw.model.network.api.GeoJsonApi
import com.weng.maphw.model.network.web.GeoJsonApiWeb
import com.weng.maphw.model.network.web.GeoJsonApiWebImpl
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {
    single<GeoJsonApi> {get<Retrofit>(GEO_JSON_RETROFIT).create(GeoJsonApi::class.java)}
    single<GeoJsonApiWeb> {GeoJsonApiWebImpl(get())}
}