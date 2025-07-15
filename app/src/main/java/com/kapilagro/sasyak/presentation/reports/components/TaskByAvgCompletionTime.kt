package com.kapilagro.sasyak.presentation.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapilagro.sasyak.presentation.common.theme.*
import java.util.Locale

@Composable
fun TaskByAvgCompletionTime(
    avgCompletionTimeByType: Map<String, Double>
) {
    // Convert days to hours (1 day = 24 hours)
    val timeInHours = avgCompletionTimeByType.mapValues { it.value * 24 }
    val totalAvg = timeInHours.values.average().let {
        if (it.isNaN()) 0.0 else String.format(Locale.US, "%.2f", it).toDouble()
    }
    val maxTime = 3.0 // Fixed max time to match the chart's 0-3h range
    val taskTypes = timeInHours.keys.toList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Average Completion Time",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray.copy(alpha = 0.1f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val padding = 40f // Space for axis labels
                    val graphHeight = height - padding
                    val graphWidth = width - padding
                    val maxTime = 3.0 // Fixed max time to match the chart's 0-3h range
                    val barWidth = graphWidth / taskTypes.size / 1.5f // Adjusted for spacing

                    // Draw Y-axis
                    drawLine(
                        color = Color.Gray,
                        start = Offset(padding, 0f),
                        end = Offset(padding, graphHeight),
                        strokeWidth = 2f
                    )

                    // Draw X-axis
                    drawLine(
                        color = Color.Gray,
                        start = Offset(padding, graphHeight),
                        end = Offset(width, graphHeight),
                        strokeWidth = 2f
                    )

                    // Draw bars and value labels
                    taskTypes.forEachIndexed { index, taskType ->
                        val barColor = when (taskType.lowercase()) {
                            "scouting" -> ScoutingIcon
                            "fuel" -> FuelIcon
                            else -> Color.Gray
                        }
                        val x = padding + (index * graphWidth / (taskTypes.size - 1))
                        val barHeight = (timeInHours[taskType]!! / maxTime * graphHeight).toFloat()
                            .coerceAtMost(graphHeight)
                        val y = graphHeight - barHeight // Start from bottom, ensure y >= 0

                        // Draw bar, constrained to graphHeight
                        drawRect(
                            color = barColor.copy(alpha = 0.3f),
                            topLeft = Offset(x - barWidth / 2, y.coerceAtLeast(0f)),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )

                        // Draw value label at top of bar
                        drawContext.canvas.nativeCanvas.drawText(
                            String.format(Locale.US, "%.2f", timeInHours[taskType]!!) + "h",
                            x,
                            (y - 10f).coerceAtLeast(0f),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 20f // Reduced text size to fit better
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }

                // X-axis labels (task types)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 20.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    taskTypes.forEach { taskType ->
                        Text(
                            text = taskType.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = Color(0xFF47569E),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Y-axis labels (time in hours)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.TopStart)
                        .padding(top = 13.dp, bottom = 44.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val steps = listOf(3.0, 2.0, 1.0, 0.0)
                    steps.forEach { value ->
                        Text(
                            text = String.format(Locale.UK, "%.1f", value) + "h",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = Color.Gray,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                timeInHours.entries.chunked(2).forEach { chunk ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        chunk.forEach { entry ->
                            val barColor = when (entry.key.lowercase()) {
                                "scouting" -> ScoutingIcon
                                "fuel" -> FuelIcon
                                else -> Color.Gray
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(barColor, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.key,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF47569E)
                                )
                            }
                        }
                        if (chunk.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}