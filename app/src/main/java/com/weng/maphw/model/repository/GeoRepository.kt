package com.weng.maphw.model.repository

import com.google.android.gms.maps.model.LatLng

interface GeoRepository {
    suspend fun getCoordinates(): Result<List<LatLng>>
}