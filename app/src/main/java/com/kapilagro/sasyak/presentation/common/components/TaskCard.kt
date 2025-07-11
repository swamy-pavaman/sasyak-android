package com.kapilagro.sasyak.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.presentation.common.theme.AgroMuted
import com.kapilagro.sasyak.presentation.common.theme.AgroMutedForeground
import com.kapilagro.sasyak.presentation.common.theme.Border
import com.kapilagro.sasyak.presentation.common.theme.FuelContainer
import com.kapilagro.sasyak.presentation.common.theme.FuelIcon
import com.kapilagro.sasyak.presentation.common.theme.ScoutingContainer
import com.kapilagro.sasyak.presentation.common.theme.ScoutingIcon
import com.kapilagro.sasyak.presentation.common.theme.SowingContainer
import com.kapilagro.sasyak.presentation.common.theme.SowingIcon
import com.kapilagro.sasyak.presentation.common.theme.SprayingContainer
import com.kapilagro.sasyak.presentation.common.theme.SprayingIcon
import com.kapilagro.sasyak.presentation.common.theme.StatusApproved
import com.kapilagro.sasyak.presentation.common.theme.StatusPending
import com.kapilagro.sasyak.presentation.common.theme.StatusRejected
import com.kapilagro.sasyak.presentation.common.theme.White
import com.kapilagro.sasyak.presentation.common.theme.YieldContainer
import com.kapilagro.sasyak.presentation.common.theme.YieldIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formats a date-time string into a compact format
 */
fun formatDateTime(dateTimeString: String): String {
    if (dateTimeString.isBlank()) return "N/A"
    return try {
        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
        )

        var date: Date? = null
        for (format in inputFormats) {
            try {
                date = format.parse(dateTimeString)
                if (date != null) break
            } catch (_: Exception) { }
        }

        date?.let {
            // Get today's date at midnight
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            // Get input date at midnight
            val inputCal = Calendar.getInstance()
            inputCal.time = it
            inputCal.set(Calendar.HOUR_OF_DAY, 0)
            inputCal.set(Calendar.MINUTE, 0)
            inputCal.set(Calendar.SECOND, 0)
            inputCal.set(Calendar.MILLISECOND, 0)

            return if (today.time == inputCal.time) {
                // Same day → show time
                val outputTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                outputTimeFormat.format(it)
            } else {
                // Different day → show date
                val outputDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                outputDateFormat.format(it)
            }
        } ?: "N/A"
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
    var firstImageUrl = getFirstImageUrl(task.imagesJson)

    if (firstImageUrl == null){
        firstImageUrl = "https://minio.kapilagro.com:9000/sasyak/placeholder.png"
    }

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

                    // kk
                    TaskTypeChip(taskType = task.taskType)
//                    Text(
//                        text = task.title,
//                        style = MaterialTheme.typography.titleSmall.copy(
//                            fontWeight = FontWeight.Medium,
//                            color = AgroDark
//                        ),
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        modifier = Modifier.weight(1f)
//                    )

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
                    horizontalArrangement = Arrangement.End,
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
//                        kk
//                    TaskTypeChip(taskType = task.taskType)
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