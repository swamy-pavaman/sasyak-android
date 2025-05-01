package com.kapilagro.sasyak.data.api.models.requests

data class UpdateProfileRequest(
    val name: String? = null,
    val phoneNumber: String? = null,
    val password: String? = null,
    val location: String? = null,
    val profileImageUrl: String? = null
)
