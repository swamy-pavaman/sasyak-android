package com.kapilagro.sasyak.data.api.mappers

import com.kapilagro.sasyak.data.db.entities.UserEntity

import com.kapilagro.sasyak.domain.models.User
import java.util.Date

// Convert Domain User to UserEntity (ADD THIS ONLY)
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = this.id,
        name = this.name,
        email = this.email,
        phoneNumber = this.phoneNumber,
        role = this.role,
        tenantId = this.tenantId,
        managerId = this.managerId,
        profileImageUrl = this.profileImageUrl,
        location = this.location,
        joinedDate = this.joinedDate as Date?,
        lastSyncedAt = Date() // Will be overridden when saving
    )
}