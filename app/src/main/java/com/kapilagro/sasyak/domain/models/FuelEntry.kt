// FuelEntry.kt
package com.kapilagro.sasyak.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class FuelEntry(
    val id: String? = null,
    val date: String,
    val vehicleType: String,
    val vehicleNumber: String,
    val fuelType: String,
    val odometerReading: String,
    val openingStock: String,
    val quantityIssued: String,
    val expectedDistance: String,
    val purpose: String,
    val driverName: String,
    val description: String? = null,
    val odometerImageUrl: String? = null,
    val requestedBy: String? = null,
    val approvedBy: String? = null,
    val status: String = "SUBMITTED", // PENDING, APPROVED, REJECTED
    val closingStock: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
