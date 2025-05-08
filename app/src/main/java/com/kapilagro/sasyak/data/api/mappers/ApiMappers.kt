package com.kapilagro.sasyak.data.api.mappers


import android.R.attr.name
import android.os.Build
import androidx.annotation.RequiresApi
import com.kapilagro.sasyak.data.api.models.responses.*
import com.kapilagro.sasyak.data.db.entities.NotificationEntity
import com.kapilagro.sasyak.data.db.entities.UserEntity
import com.kapilagro.sasyak.domain.models.*
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date


import com.kapilagro.sasyak.data.api.models.responses.AuthResponse as ApiAuthResponse
import com.kapilagro.sasyak.domain.models.AuthResponse as DomainAuthResponse

fun ApiAuthResponse.toDomainModel(): DomainAuthResponse {
    return DomainAuthResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
        email = email,
        name = name?:"user",
        userId = userId?:0,
        role = role?:"supervisor"
    )
}

fun UserResponse.toDomainModel(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber ?: "",
        role = role,
        tenantId = tenantId,
        managerId = managerId,
        profileImageUrl = profile, // mapping profile to profileImageUrl
        location = location,
        joinedDate = null,         // not present in response; set as null
        specialization = emptyList() // not present in response; set as empty list
    )
}

fun TaskResponse.toDomainModel(): Task {
    return Task(
        id = id,
        title = taskType, // Using taskType as title for display purposes
        description = description,
        status = status,
        taskType = taskType,
        createdBy = createdBy,
        assignedTo = assignedTo,
        createdAt = createdAt,
        updatedAt = updatedAt,
        detailsJson = detailsJson,
        imagesJson = imagesJson,
        implementationJson = implementationJson
    )
}

fun TaskAdviceResponse.toDomainModel(): TaskAdvice {
    return TaskAdvice(
        id = id,
        taskId = taskId,
        managerName = managerName,
        adviceText = adviceText,
        createdAt = createdAt
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun NotificationResponse.toDomainModel(): Notification {
    // Calculate time ago string
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val notificationTime = try {
        ZonedDateTime.parse(createdAt, formatter)
    } catch (e: Exception) {
        ZonedDateTime.now().minusHours(1) // Fallback if parsing fails
    }
    val now = ZonedDateTime.now()

    val timeAgo = when {
        ChronoUnit.MINUTES.between(notificationTime, now) < 60 -> {
            val minutes = ChronoUnit.MINUTES.between(notificationTime, now)
            if (minutes <= 1) "Just now" else "$minutes minutes ago"
        }
        ChronoUnit.HOURS.between(notificationTime, now) < 24 -> {
            val hours = ChronoUnit.HOURS.between(notificationTime, now)
            "$hours ${if (hours == 1L) "hour" else "hours"} ago"
        }
        ChronoUnit.DAYS.between(notificationTime, now) < 7 -> {
            val days = ChronoUnit.DAYS.between(notificationTime, now)
            "$days ${if (days == 1L) "day" else "days"} ago"
        }
        ChronoUnit.WEEKS.between(notificationTime, now) < 4 -> {
            val weeks = ChronoUnit.WEEKS.between(notificationTime, now)
            "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
        }
        else -> {
            val formattedDate = notificationTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            formattedDate
        }
    }

    return Notification(
        id = id,
        title = title,
        message = message,
        taskId = taskId,
        isRead = isRead,
        createdAt = createdAt,
        timeAgo = timeAgo
    )
}

fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        role = role,
        tenantId = tenantId,
        managerId = managerId,
        profileImageUrl = profileImageUrl,
        location = location,
        joinedDate = joinedDate?.let { it.toString() }
    )
}

fun User.toEntityModel(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        role = role,
        tenantId = tenantId,
        managerId = managerId,
        profileImageUrl = profileImageUrl,
        location = location,
        joinedDate = joinedDate?.let {
            try {
                Date(it)
            } catch (e: Exception) {
                null
            }
        }
    )
}

