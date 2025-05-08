package com.kapilagro.sasyak.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.presentation.common.theme.*
import java.text.SimpleDateFormat
import java.util.*



/**
 * Formats a date-time string into a readable format
 *
 * @param dateTimeString The date-time string in ISO format (yyyy-MM-dd'T'HH:mm:ss)
 * @return Formatted date-time string (e.g., "May 8, 2025 • 10:30 AM")
 */
fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        date?.let { outputFormat.format(it) } ?: "N/A"
    } catch (e: Exception) {
        "N/A"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            focusedElevation = 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Card,
            contentColor = Foreground
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with task title and icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AgroDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Task type chip moved next to title for better visual hierarchy
                TaskTypeChip(taskType = task.taskType)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status indicator bar
            StatusIndicator(status = task.status ?: "pending")

            Spacer(modifier = Modifier.height(12.dp))

            // Description with subtle background
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                color = AgroMuted
            ) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Foreground,
                    modifier = Modifier.padding(8.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer with date and action icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AgroMutedForeground
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formatDateTime(task.createdAt ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = AgroMutedForeground
                    )
                }

                // View details button with primary color
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = AgroLight,
                        contentColor = AgroPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "View details",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskTypeChip(taskType: String) {
    val (backgroundColor, textColor) = when (taskType.lowercase()) {
        "scouting" -> Pair(ScoutingContainer, ScoutingIcon)
        "spraying" -> Pair(SprayingContainer, SprayingIcon)
        "sowing" -> Pair(SowingContainer, SowingIcon)
        "fuel" -> Pair(FuelContainer, FuelIcon)
        "yield" -> Pair(YieldContainer, YieldIcon)
        else -> Pair(AgroMuted, AgroMutedForeground)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier.height(28.dp)
    ) {
        Text(
            text = taskType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun StatusIndicator(status: String) {
    val (color, text) = when (status.lowercase()) {
        "pending" -> Pair(StatusPending, "Pending")
        "approved" -> Pair(StatusApproved, "Approved")
        "rejected" -> Pair(StatusRejected, "Rejected")
        else -> Pair(Color.Gray, status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.height(26.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = color,
                modifier = Modifier.size(8.dp)
            ) {}

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = color
            )
        }
    }
}