package com.kapilagro.sasyak.presentation.common.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.presentation.common.theme.Green500
import com.kapilagro.sasyak.presentation.common.theme.StatusApproved
import com.kapilagro.sasyak.presentation.common.theme.StatusRejected

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val (indicatorColor, backgroundColor) = when (notification.title) {
        "Task Approved" -> Pair(StatusApproved, StatusApproved.copy(alpha = 0.1f))
        "Task Rejected" -> Pair(StatusRejected, StatusRejected.copy(alpha = 0.1f))
        "New Advice" -> Pair(Green500, Green500.copy(alpha = 0.1f))
        else -> Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Timestamp
            Text(
                text = notification.timeAgo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}
internal fun formatDateTime(dateTimeString: String): String {
    // Placeholder implementation
    // In a real app, parse the date string and format it
    return dateTimeString
}