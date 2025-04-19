package com.kapilagro.sasyak.data.api.models.requests

data class UpdateTaskStatusRequest(
    val status: String,
    val comment: String? = null
)