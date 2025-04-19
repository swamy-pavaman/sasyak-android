package com.kapilagro.sasyak.data.api.models.responses

data class TaskReportResponse(
    val totalTasks: Int,
    val tasksByType: Map<String, Int>,
    val tasksByStatus: Map<String, Int>,
    val tasksByUser: Map<String, Int>,
    val avgCompletionTimeByType: Map<String, Double>
)