package com.kapilagro.sasyak.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class YieldDetails(
    val harvestDate: String,
    val cropName: String,
    val row: String,
    val valveName: String,
    val fieldArea: String? = null,
    val yieldQuantity: String,
    val yieldUnit: String,
    val qualityGrade: String? = null,
    val moistureContent: String? = null,
    val harvestMethod: String? = null,
    val notes: String? = null,
    val dueDate: String? = null,
    val latitude : Double? = null,
    val longitude : Double? = null
   // val uploadedFiles: List<String>? = null
)