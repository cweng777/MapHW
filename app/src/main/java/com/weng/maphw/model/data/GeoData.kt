package com.weng.maphw.model.data

/**
 * 接收GeoJson api回傳資料
 */
data class GeoData(
    val features: List<Feature>?,
    val type: String?
)