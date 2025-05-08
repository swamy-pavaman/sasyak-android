//package com.kapilagro.sasyak.domain.models
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class SowingDetails(
//    val sowingDate: String,
//    val cropName: String,
//    val fieldId: String,
//    val areaSize: Double,
//    val seedVariety: String,
//    val seedQuantity: Double,
//    val seedUnit: String,
//    val spacingBetweenPlants: String? = null,
//    val spacingBetweenRows: String? = null,
//    val soilCondition: String? = null,
//    val weatherCondition: String? = null,
//    val uploadedFiles: List<String>? = null
//)
package com.kapilagro.sasyak.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class SowingDetails(
    val sowingDate: String,
    val cropName: String,
    val row: Int,
    val fieldArea: String? = null,
    val seedVariety: String,
    val seedQuantity: String? = null,
    val seedUnit: String? = null,
    val sowingMethod: String,
    val seedTreatment: String? = null,
    val spacingBetweenRows: String? = null,
    val spacingBetweenPlants: String? = null,
    val soilCondition: String? = null,
    val weatherCondition: String? = null,
    val uploadedFiles: List<String>? = null
)