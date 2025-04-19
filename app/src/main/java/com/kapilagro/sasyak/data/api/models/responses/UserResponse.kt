package com.kapilagro.sasyak.data.api.models.responses

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val tenantId: String,
    val phoneNumber: String? = null,
    val managerId: Int? = null
)