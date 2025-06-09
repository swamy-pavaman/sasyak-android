package com.kapilagro.sasyak.presentation.reports.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kapilagro.sasyak.domain.models.DailyTaskCount
import com.kapilagro.sasyak.presentation.common.theme.Info
import com.kapilagro.sasyak.presentation.common.theme.SowingIcon

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskCompletionChart(
    taskCounts: Pair<List<DailyTaskCount>, List<DailyTaskCount>>,
    modifier: Modifier = Modifier
) {
    val completedTasks = taskCounts.first
    val createdTasks = taskCounts.second
    val maxCount = maxOf(
        completedTasks.maxOfOrNull { it.count } ?: 0,
        createdTasks.maxOfOrNull { it.count } ?: 0,
        1 // Ensure minimum value to avoid division by zero
    )
    val chartHeight = 200.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Info, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Completed",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(SowingIcon, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Created",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .height(chartHeight)
                    .align(Alignment.CenterStart),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in maxCount downTo 0 step (maxCount / 4).coerceAtLeast(1)) {
                    Text(
                        text = "$i",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    if (i > 0) Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Chart and grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .padding(start = 32.dp, top = 8.dp, bottom = 24.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val barGroupWidth = (size.width - 24.dp.toPx()) / completedTasks.size.coerceAtLeast(1)
                    val barWidth = barGroupWidth * 0.4f // 40% width for each bar, leaving space between
                    val spacing = barGroupWidth * 0.2f // 20% spacing between bars in a group

                    // Draw grid lines
                    for (i in 0..4) {
                        val y = size.height - (size.height * i / 4)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw bars for completed and created tasks
                    completedTasks.forEachIndexed { index, dailyCount ->
                        val completedHeight = if (maxCount > 0) {
                            (dailyCount.count.toFloat() / maxCount) * size.height
                        } else 0f

                        val createdCount = createdTasks.getOrNull(index)?.count ?: 0
                        val createdHeight = if (maxCount > 0) {
                            (createdCount.toFloat() / maxCount) * size.height
                        } else 0f

                        val groupX = (index * barGroupWidth) + 12.dp.toPx()

                        // Completed tasks bar
                        drawRoundRect(
                            color = Info,
                            topLeft = Offset(groupX, size.height - completedHeight),
                            size = Size(barWidth, completedHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )

                        // Created tasks bar
                        drawRoundRect(
                            color = SowingIcon,
                            topLeft = Offset(groupX + barWidth + spacing, size.height - createdHeight),
                            size = Size(barWidth, createdHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                    }
                }

                // X-axis labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    completedTasks.forEach { dailyCount ->
                        Text(
                            text = dailyCount.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}