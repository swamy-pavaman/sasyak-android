package com.kapilagro.sasyak.data.api.models.requests

data class FilterRequest(
    val status: String,
    val page: Int,
    val size: Int,
    val sortBy: String?=null,
    val sortDirection: String?=null,
    val managerId : Int? = null,
    val currentUserTasks : Boolean? = null,
    val taskType : String?=null,
)
