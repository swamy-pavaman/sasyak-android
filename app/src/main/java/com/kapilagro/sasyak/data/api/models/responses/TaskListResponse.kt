package com.kapilagro.sasyak.data.api.models.responses

data class TaskListResponse(
    val tasks: List<TaskResponse>,
    val totalCount: Int
)