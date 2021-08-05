package com.weng.maphw.model.network.web

import com.weng.maphw.extension.checkResponseBody
import com.weng.maphw.model.data.GeoData
import com.weng.maphw.model.network.api.GeoJsonApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeoJsonApiWebImpl(
    private val geoJsonApi: GeoJsonApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): GeoJsonApiWeb {

    override suspend fun getGeoJsonData(): Result<GeoData> = withContext(ioDispatcher) {
        kotlin.runCatching {
            geoJsonApi.getGeoJsonData().checkResponseBody()
        }
    }

}