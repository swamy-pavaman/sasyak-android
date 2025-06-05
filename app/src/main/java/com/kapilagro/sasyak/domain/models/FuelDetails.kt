package com.kapilagro.sasyak.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class FuelDetails(
    val fuelDate: String,
    val vehicleName: String,
    val vehicleNumber: String? = null,
    val fuelType: String,
    val quantity: String,
    val unit: String,
    val costPerUnit: String? = null,
    val totalCost: String? = null,
    val driverName: String? = null,
    val odometer: String? = null,
    val purposeOfFuel: String? = null,
    val refillLocation: String? = null,
    val notes: String? = null,
   // val uploadedFiles: List<String>? = null
)