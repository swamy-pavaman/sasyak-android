package com.kapilagro.sasyak.domain.models

data class TaskReport(
    val totalTasks: Int,
    val tasksByType: Map<String, Int>,
    val tasksByStatus: Map<String, Int>,
    val tasksByUser: Map<String, Int>,
    val avgCompletionTimeByType: Map<String, Double>
)