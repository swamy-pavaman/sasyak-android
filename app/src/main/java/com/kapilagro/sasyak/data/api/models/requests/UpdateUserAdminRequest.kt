package com.kapilagro.sasyak.data.api.models.requests

data class UpdateUserAdminRequest(
    val name: String? = null,
    val email: String? = null,
    val phone_number: String? = null,
    val role: String? = null,
    val password: String? = null,
    val managerId: Int? = null
)