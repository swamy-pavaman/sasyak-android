package com.kapilagro.sasyak.data.api.models.responses

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val email: String,
    val name: String?,
    val userId: Int?,
    val role:String?  //TODO change nullable into nonnull: javax.annotation.Nonnull change in backend to send name and userid and role from refreshToken: kotlin.String
)
