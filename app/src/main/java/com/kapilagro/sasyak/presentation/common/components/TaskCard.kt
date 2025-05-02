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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.presentation.common.theme.StatusApproved
import com.kapilagro.sasyak.presentation.common.theme.StatusPending
import com.kapilagro.sasyak.presentation.common.theme.StatusRejected

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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formatDateTime(task.createdAt ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TaskTypeChip(taskType = task.taskType)
            }
        }
    }
}

@Composable
fun TaskTypeChip(taskType: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when (taskType.lowercase()) {
            "scouting" -> Color(0xFFE3F2FD)
            "spraying" -> Color(0xFFE8F5E9)
            "sowing" -> Color(0xFFF3E5F5)
            "fuel" -> Color(0xFFFFF8E1)
            "yield" -> Color(0xFFFCE4EC)
            else -> Color(0xFFEEEEEE)
        },
        modifier = Modifier.height(28.dp)
    ) {
        Text(
            text = taskType,
            style = MaterialTheme.typography.labelMedium,
            color = when (taskType.lowercase()) {
                "scouting" -> Color(0xFF1976D2)
                "spraying" -> Color(0xFF2E7D32)
                "sowing" -> Color(0xFF7B1FA2)
                "fuel" -> Color(0xFFFFA000)
                "yield" -> Color(0xFFD81B60)
                else -> Color(0xFF616161)
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatusIndicator(status: String) {
    val (color, text) = when (status.lowercase()) {
        "pending" -> Pair(StatusPending, "Pending")
        "approved" -> Pair(StatusApproved, "Approved")
        "rejected" -> Pair(StatusRejected, "Rejected")
        else -> Pair(Color.Gray, status)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.height(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = color,
                modifier = Modifier.size(8.dp)
            ) {}

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}