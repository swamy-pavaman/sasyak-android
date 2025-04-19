package com.kapilagro.sasyak.data.api.models.responses

data class SearchResultsResponse(
    val tasks: List<TaskResponse>,
    val users: List<UserResponse>,
    val totalResults: Int
)