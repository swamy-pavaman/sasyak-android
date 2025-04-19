package com.kapilagro.sasyak.data.api.models.requests

data class CreateAdviceRequest(
    val taskId: Int,
    val adviceText: String
)