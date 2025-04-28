package com.kapilagro.sasyak.data.api.models.responses

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val email: String,
    val name: String,
    val userId: Int,
    val role:String
)
