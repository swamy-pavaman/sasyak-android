package com.kapilagro.sasyak.domain.models

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val role: String, // "SUPER_ADMIN", "ADMIN", "MANAGER", "SUPERVISOR"
    val tenantId: String,
    val managerId: Int? = null,
    val profileImageUrl: String? = null,
    val location: String? = null,
    val joinedDate: String? = null,
    val specialization: List<String> = emptyList()
)