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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kapilagro.sasyak.domain.models.DailyTaskCount
import com.kapilagro.sasyak.presentation.common.theme.Info
import com.kapilagro.sasyak.presentation.common.theme.ScoutingIcon
import com.kapilagro.sasyak.presentation.common.theme.SowingIcon
import com.kapilagro.sasyak.presentation.common.theme.SprayingIcon

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
        1
    )
    val chartHeight = 220.dp
    val yAxisWidth = 36.dp

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
                        .background(SprayingIcon, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Completed", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(ScoutingIcon, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Created", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight + 40.dp) // Extra for X-axis labels
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .width(yAxisWidth)
                    .height(chartHeight-40.dp) // Match Canvas height
                    .padding(end = 8.dp), // Add padding for spacing
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in maxCount downTo 0 step (maxCount / 4).coerceAtLeast(1)) {
                    Text(
                        text = "$i",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically) // Center vertically
                    )
                }
            }

            // Chart + X-axis labels
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .height(chartHeight -40.dp)
                ) {
                    val barGroupWidth = size.width / completedTasks.size.coerceAtLeast(1)
                    val barWidth = barGroupWidth * 0.4f
                    val spacing = barGroupWidth * 0.2f

                    // Draw grid lines
                    for (i in 0..4) {
                        val y = size.height * (1 - i / 4f) // Adjusted for better alignment
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw bars
                    completedTasks.forEachIndexed { index, dailyCount ->
                        val completedHeight = (dailyCount.count.toFloat() / maxCount) * size.height
                        val createdCount = createdTasks.getOrNull(index)?.count ?: 0
                        val createdHeight = (createdCount.toFloat() / maxCount) * size.height
                        val groupX = index * barGroupWidth

                        // Completed tasks bar
                        drawRoundRect(
                            color = SprayingIcon,
                            topLeft = Offset(groupX, size.height - completedHeight),
                            size = Size(barWidth, completedHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        // Created tasks bar
                        drawRoundRect(
                            color = ScoutingIcon,
                            topLeft = Offset(groupX + barWidth + spacing, size.height - createdHeight),
                            size = Size(barWidth, createdHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }

                // X-axis labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    completedTasks.forEachIndexed { index, dailyCount ->
                        Text(
                            text = dailyCount.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f / completedTasks.size) // Evenly distribute
                                .wrapContentWidth(align = Alignment.CenterHorizontally) // Center in group
                        )
                    }
                }
            }
        }
    }
}
