package com.kapilagro.sasyak.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val tenantId: String,
    val managerId: Int?,
    val profileImageUrl: String?,
    val location: String?,
    val joinedDate: Date?,
    val lastSyncedAt: Date = Date()
)