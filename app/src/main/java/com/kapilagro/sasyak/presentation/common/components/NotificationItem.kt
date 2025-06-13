package com.kapilagro.sasyak.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.presentation.common.theme.Green500
import com.kapilagro.sasyak.presentation.common.theme.StatusApproved
import com.kapilagro.sasyak.presentation.common.theme.StatusRejected
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    // Compute timeAgo from createdAt
    val timeAgo = notification.createdAt?.let { computeTimeAgo(it) } ?: "Just now"

    // Determine colors based on title, with null safety
    val (indicatorColor, backgroundColor) = when (notification.title) {
        "Task Approved" -> Pair(StatusApproved, StatusApproved.copy(alpha = 0.1f))
        "Task Rejected" -> Pair(StatusRejected, StatusRejected.copy(alpha = 0.1f))
        "New Advice" -> Pair(Green500, Green500.copy(alpha = 0.1f))
        else -> Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick), // Make the Surface clickable
        shape = RoundedCornerShape(12.dp),
        color = if (notification.isRead) Color.White else backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            // Left indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .background(if (notification.isRead) Color.Transparent else indicatorColor)
            )

            // Content
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = notification.title ?: "No Title", // Null safety for title
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold // Bold for unread
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message ?: "No Message", // Null safety for message
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }

            // Timestamp
            Text(
                text = timeAgo, // Use computed timeAgo
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}

// Utility function to compute timeAgo from createdAt
@Composable
fun computeTimeAgo(createdAt: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = try {
        dateFormat.parse(createdAt) ?: Date()
    } catch (e: Exception) {
        Date()
    }
    val diff = System.currentTimeMillis() - date.time
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000} minutes ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
        else -> "${diff / 86_400_000} days ago"
    }
}