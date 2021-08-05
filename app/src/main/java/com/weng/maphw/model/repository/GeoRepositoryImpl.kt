package com.weng.maphw.model.repository

import com.google.android.gms.maps.model.LatLng
import com.weng.maphw.model.network.web.GeoJsonApiWeb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeoRepositoryImpl(
    private val geoJsonApiWeb: GeoJsonApiWeb
): GeoRepository {

    override suspend fun getCoordinates(): Result<List<LatLng>> = withContext(Dispatchers.Default) {
        geoJsonApiWeb.getGeoJsonData().mapCatching {
            val getList = mutableListOf<LatLng>()
            if (it.features?.firstOrNull()?.geometry?.coordinates?.firstOrNull()?.isNotEmpty() == true) {
                val coordinatesList = it.features.firstOrNull()?.geometry?.coordinates?.first()
                if (coordinatesList != null) {
                    for(coordinates in coordinatesList) {
                        getList.add(LatLng(coordinates[1], coordinates[0]))
                    }
                }
            }
            getList
        }
    }
}