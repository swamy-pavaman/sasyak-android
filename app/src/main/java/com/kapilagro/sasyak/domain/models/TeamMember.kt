// TeamMember.kt
package com.kapilagro.sasyak.domain.models

data class TeamMember(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val location: String? = null
)