package com.kapilagro.sasyak.data.api.models.responses

data class TaskResponse(
    val id: Int,
    val taskType: String,
    val description: String,
    val status: String,
    val createdBy: String,
    val assignedTo: String?,
    val createdAt: String,
    val updatedAt: String,
    val detailsJson: String?,
    val imagesJson: String?,
    val implementationJson: String?
)