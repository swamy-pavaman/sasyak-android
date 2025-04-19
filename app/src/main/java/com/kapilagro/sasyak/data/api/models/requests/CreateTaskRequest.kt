package com.kapilagro.sasyak.data.api.models.requests


data class CreateTaskRequest(
    val taskType: String,
    val description: String,
    val detailsJson: String? = null,
    val imagesJson: String? = null,
    val assignedToId: Int? = null
)