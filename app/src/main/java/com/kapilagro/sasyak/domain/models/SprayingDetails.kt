package com.kapilagro.sasyak.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class SprayingDetails(
    val sprayingDate: String,
    val cropName: String,
    val row: String,
    val fieldArea: String? = null,
    val chemicalName: String,
    val dosage: String? = null,
    val sprayingMethod: String,
    val nutrients: String? = null,
    val disease: String? = null,
    val pest: String? = null,
    val weatherCondition: String? = null,
    val valveName: String? = null,
    val dueDate: String? = null,
    val latitude : Double? = null,
    val longitude : Double? = null
   // val uploadedFiles: List<String>? = null
)