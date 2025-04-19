package com.kapilagro.sasyak.domain.models

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val email: String,
    val name: String,
    val userId: Int
)