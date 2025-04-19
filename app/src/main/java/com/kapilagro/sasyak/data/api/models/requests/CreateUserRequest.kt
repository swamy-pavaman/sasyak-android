package com.kapilagro.sasyak.data.api.models.requests

data class CreateUserRequest(
    val name: String,
    val email: String,
    val phone_number: String,
    val role: String,
    val managerId: Int? = null
)