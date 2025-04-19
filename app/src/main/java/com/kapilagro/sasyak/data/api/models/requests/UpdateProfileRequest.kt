package com.kapilagro.sasyak.data.api.models.requests

data class UpdateProfileRequest(
    val name: String? = null,
    val phone_number: String? = null,
    val password: String? = null
)