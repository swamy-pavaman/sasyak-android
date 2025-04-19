package com.kapilagro.sasyak.data.api.models.responses
data class TaskAdviceResponse(
    val id: Int,
    val taskId: Int,
    val managerName: String,
    val adviceText: String,
    val createdAt: String
)