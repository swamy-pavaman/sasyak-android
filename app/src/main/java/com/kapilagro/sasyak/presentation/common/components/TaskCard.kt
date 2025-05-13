package com.kapilagro.sasyak.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * @return Formatted date-time string (e.g., "Today, 10:30 AM" or "May 10, 11:20 AM")
 */
fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString) ?: return "N/A"

        val today = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply { time = date }

        val isToday = today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        return if (isToday) {
            "Today, ${timeFormat.format(date)}"
        } else {
            "${dateFormat.format(date)}, ${timeFormat.format(date)}"
        }
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
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        border = BorderStroke(0.5.dp, Border),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title row with status icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Only show the task title without taskType
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AgroDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Status indicator icon
                val statusIcon = when (task.status?.lowercase()) {
                    "pending" -> Icons.Outlined.Schedule
                    "approved" -> Icons.Outlined.CheckCircle
                    "rejected" -> Icons.Outlined.Close
                    else -> Icons.Outlined.Schedule
                }

                val statusColor = when (task.status?.lowercase()) {
                    "pending" -> StatusPending
                    "approved" -> StatusApproved
                    "rejected" -> StatusRejected
                    else -> StatusPending
                }

                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Task status: ${task.status}",
                    tint = statusColor,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "View details",
                    tint = Border,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = AgroMutedForeground,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row with date and task type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date with icon
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

                // Task type chip on the right
                TaskTypeChip(taskType = task.taskType)
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
        modifier = Modifier.height(24.dp)
    ) {
        Text(
            text = taskType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}