package com.weng.maphw.model.network.api

import com.weng.maphw.model.data.GeoData
import retrofit2.Response
import retrofit2.http.GET

interface GeoJsonApi {

    @GET("ronnywang/100年全國縣市界圖/uri/23238645?format=geojson")
    suspend fun getGeoJsonData(): Response<GeoData>
}