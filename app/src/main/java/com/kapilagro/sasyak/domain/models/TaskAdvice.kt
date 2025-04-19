package com.kapilagro.sasyak.domain.models
data class TaskAdvice(
    val id: Int,
    val taskId: Int,
    val managerName: String,
    val adviceText: String,
    val createdAt: String
)