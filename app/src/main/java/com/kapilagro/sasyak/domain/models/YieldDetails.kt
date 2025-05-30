//package com.kapilagro.sasyak.domain.models
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class YieldDetails(
//    val harvestDate: String,
//    val cropName: String,
//    val fieldId: String,
//    val areaSize: Double,
//    val harvestedQuantity: Double,
//    val yieldUnit: String,
//    val grainMoisture: String? = null,
//    val grainQuality: String? = null,
//    val harvestMethod: String? = null,
//    val laborHours: Double? = null,
//    val fuelUsed: Double? = null,
//    val fuelUnit: String? = null,
//    val weatherCondition: String? = null,
//    val notes: String? = null,
//    val uploadedFiles: List<String>? = null
//)


package com.kapilagro.sasyak.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class YieldDetails(
    val harvestDate: String,
    val cropName: String,
    val row: Int,
    val fieldArea: String? = null,
    val yieldQuantity: String,
    val yieldUnit: String,
    val qualityGrade: String? = null,
    val moistureContent: String? = null,
    val harvestMethod: String? = null,
    val notes: String? = null,
   // val uploadedFiles: List<String>? = null
)