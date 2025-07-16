package com.kapilagro.sasyak.data.api.models.requests

data class FilterRequest(
    val status: String,
    val page: Int,
    val size: Int,
    val sortBy: String,
    val sortDirection: String,
    val managerId : Int? = null,
)
