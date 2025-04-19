package com.kapilagro.sasyak.data.api.models.responses

data class TaskDetailResponse(
    val task: TaskResponse,
    val advices: List<TaskAdviceResponse>
)