package com.kapilagro.sasyak.data.api.models.responses

data class PagedUserListResponse(
    val employees: List<UserResponse>,
    val totalItems: Long,
    val totalPages: Int,
    val currentPage: Int
)