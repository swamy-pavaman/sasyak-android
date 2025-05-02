package com.kapilagro.sasyak.domain.models

import java.time.LocalDateTime

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val status: String, // "pending", "approved", "rejected", "implemented"
    val taskType: String, // "scouting", "spraying", "sowing", "fuel", "yield"
    val createdBy: String,
    val assignedTo: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val detailsJson: String?,
    val imagesJson: String?,
    val implementationJson: String?
)