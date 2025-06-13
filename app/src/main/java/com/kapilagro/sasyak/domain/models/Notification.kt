//package com.kapilagro.sasyak.domain.models
//data class Notification(
//    val id: Int,
//    val title: String,
//    val message: String,
//    val taskId: Int?,
//    val isRead: Boolean,
//    val createdAt: String,
//    val timeAgo: String // Computed field for display purposes
//)
package com.kapilagro.sasyak.domain.models

import java.util.Date

data class Notification(
    val id: Int,
    val title: String?,
    val message: String?,
    val taskId: Int?,
    val isRead: Boolean,
    val createdAt: String, // Likely a String in the domain model, causing the mismatch
    val timeAgo: String? = null // Additional field causing the "no value passed" error
)