package com.kapilagro.sasyak.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.presentation.common.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Formats a date-time string into a compact format
 */
fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString) ?: return "N/A"
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        timeFormat.format(date)
    } catch (e: Exception) {
        "N/A"
    }
}

/**
 * Extracts the first image URL from the imagesJson string
 */
fun getFirstImageUrl(imagesJson: String?): String? {
    return try {
        if (imagesJson.isNullOrBlank()) return null
        val gson = Gson()
        val listType = object : TypeToken<List<String>>() {}.type
        val imagesList: List<String> = gson.fromJson(imagesJson, listType)
        imagesList.firstOrNull()
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    val firstImageUrl = getFirstImageUrl(task.imagesJson)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        border = BorderStroke(0.3.dp, Border),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Image (if available)
            firstImageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Task image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AgroMuted)
            )

            // Content column
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title and status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium,
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
                        "submitted" -> Icons.Outlined.Schedule
                        "implemented" -> Icons.Outlined.CheckCircle
                        else -> Icons.Outlined.Schedule
                    }

                    val statusColor = when (task.status?.lowercase()) {
                        "pending" -> StatusPending
                        "approved" -> StatusApproved
                        "rejected" -> StatusRejected
                        "submitted" -> StatusPending
                        "implemented" -> StatusApproved
                        else -> StatusPending
                    }

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "Task status: ${task.status}",
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Description
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgroMutedForeground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Time and task type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AgroMutedForeground
                        )
                        Text(
                            text = formatDateTime(task.createdAt ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = AgroMutedForeground
                        )
                    }

                    TaskTypeChip(taskType = task.taskType)
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
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.height(24.dp)
    ) {
        Text(
            text = taskType.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            },
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}