// TeamMemberResponse.kt
package com.kapilagro.sasyak.data.api.models.responses

data class TeamMemberResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val profileImage: String? = null,
    val phoneNumber: String? = null,
    val location: String? = null
)

