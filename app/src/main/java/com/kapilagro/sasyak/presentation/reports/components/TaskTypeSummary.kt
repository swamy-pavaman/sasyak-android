package com.kapilagro.sasyak.presentation.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kapilagro.sasyak.presentation.common.theme.*

@Composable
fun TaskTypeSummary(
    tasksByType: Map<String, Int>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        tasksByType.entries.forEachIndexed { index, entry ->
            val (taskType, count) = entry
            val totalTasks = tasksByType.values.sum()
            val percentage = if (totalTasks > 0) count * 100f / totalTasks else 0f

            val barColor = when (taskType.lowercase()) {
                "scouting" -> ScoutingIcon
                "spraying" -> SprayingIcon
                "sowing" -> SowingIcon
                "fuel" -> FuelIcon
                "yield" -> YieldIcon
                else -> Color.Gray
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(barColor, shape = MaterialTheme.shapes.small)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = taskType,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(100.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentage / 100f)
                            .background(barColor, MaterialTheme.shapes.small)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (index < tasksByType.size - 1) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}