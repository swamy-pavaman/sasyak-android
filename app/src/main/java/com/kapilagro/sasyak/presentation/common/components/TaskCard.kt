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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstImageUrl = getFirstImageUrl(task.imagesJson)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = TaskCardBackground // Updated to new color
        ),
        border = BorderStroke(0.5.dp, CardBorder),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            firstImageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Task image",
                    modifier = Modifier
                        .size(90.dp),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AgroMuted)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskTypeChip(taskType = task.taskType)

                        val statusIcon = when (task.status?.lowercase()) {
                            "pending" -> Icons.Outlined.Schedule
                            "approved" -> Icons.Outlined.CheckCircle
                            "rejected" -> Icons.Outlined.Close
                            "submitted" -> Icons.Outlined.Schedule
                            "implemented" -> Icons.Outlined.CheckCircle
                            else -> Icons.Outlined.Schedule
                        }

                        val statusColor = when (task.status?.lowercase()) {
                            "pending" -> WarningAccent
                            "approved" -> PrimaryAccent
                            "rejected" -> ErrorAccent
                            "submitted" -> WarningAccent
                            "implemented" -> PrimaryAccent
                            else -> WarningAccent
                        }

                        Icon(
                            imageVector = statusIcon,
                            contentDescription = "Task status: ${task.status}",
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TextSecondary
                        )
                        Text(
                            text = formatDateTime(task.createdAt ?: ""),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskTypeChip(taskType: String) {
    val (backgroundColor, textColor) = when (taskType.lowercase()) {
        "scouting" -> Pair(ScoutingColor.copy(alpha = 0.1f), ScoutingColor)
        "spraying" -> Pair(SprayingColor.copy(alpha = 0.1f), SprayingColor)
        "sowing" -> Pair(SowingColor.copy(alpha = 0.1f), SowingColor)
        "fuel" -> Pair(FuelColor.copy(alpha = 0.1f), FuelColor)
        "yield" -> Pair(YieldColor.copy(alpha = 0.1f), YieldColor)
        else -> Pair(AgroMuted.copy(alpha = 0.1f), AgroMutedForeground)
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