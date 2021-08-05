package com.weng.maphw.model.network.web

import com.weng.maphw.model.data.GeoData

interface GeoJsonApiWeb {
    /**
     * 取得 繪製於地圖 (Polygon) 的 GeoJson 資料
     */
    suspend fun getGeoJsonData (): Result<GeoData>
}