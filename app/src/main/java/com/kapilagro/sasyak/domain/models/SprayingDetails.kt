//package com.kapilagro.sasyak.domain.models
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class SprayingDetails(
//    val sprayingDate: String,
//    val cropName: String,
//    val fieldId: String,
//    val areaSize: Double,
//    val pesticideName: String,
//    val pesticideQuantity: Double,
//    val pesticideUnit: String,
//    val waterQuantity: Double,
//    val waterUnit: String,
//    val targetPests: String? = null,
//    val weatherCondition: String? = null,
//    val temperature: String? = null,
//    val windSpeed: String? = null,
//    val uploadedFiles: List<String>? = null
//)
package com.kapilagro.sasyak.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class SprayingDetails(
    val sprayingDate: String,
    val cropName: String,
    val row: Int,
    val fieldArea: String? = null,
    val chemicalName: String,
    val dosage: String? = null,
    val sprayingMethod: String,
    val targetPest: String? = null,
    val weatherCondition: String? = null,
   // val uploadedFiles: List<String>? = null
)