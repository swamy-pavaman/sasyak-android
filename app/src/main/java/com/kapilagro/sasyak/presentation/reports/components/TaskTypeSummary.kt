package com.kapilagro.sasyak.presentation.reports.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapilagro.sasyak.presentation.common.theme.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.CardColors


//@Composable
//fun TaskTypeSummary(
//    tasksByType: Map<String, Int>
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp)
//    ) {
//        tasksByType.entries.forEachIndexed { index, entry ->
//            val (taskType, count) = entry
//            val totalTasks = tasksByType.values.sum()
//            val percentage = if (totalTasks > 0) count * 100f / totalTasks else 0f
//
//            val barColor = when (taskType.lowercase()) {
//                "scouting" -> ScoutingIcon
//                "spraying" -> SprayingIcon
//                "sowing" -> SowingIcon
//                "fuel" -> FuelIcon
//                "yield" -> YieldIcon
//                else -> Color.Gray
//            }
//
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(12.dp)
//                        .background(barColor, shape = MaterialTheme.shapes.small)
//                )
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                Text(
//                    text = taskType,
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.width(100.dp)
//                )
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(8.dp)
//                        .background(Color.LightGray.copy(alpha = 0.3f), MaterialTheme.shapes.small)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .fillMaxWidth(percentage / 100f)
//                            .background(barColor, MaterialTheme.shapes.small)
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(16.dp))
//
//                Text(
//                    text = count.toString(),
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            if (index < tasksByType.size - 1) {
//                Divider(
//                    modifier = Modifier.padding(vertical = 8.dp),
//                    color = MaterialTheme.colorScheme.surfaceVariant
//                )
//            }
//        }
//    }
//}

@Composable
fun TaskTypeSummary(
    data: Map<String, Int>
) {
    val total = data.values.sum()
    val angles = data.values.map { it / total.toFloat() * 360f }

    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    var startAngle = -90f
                    data.keys.forEachIndexed { index, taskType ->
                        val barColor = when (taskType.lowercase()) {
                            "scouting" -> ScoutingIcon
                            "spraying" -> SprayingIcon
                            "sowing" -> SowingIcon
                            "fuel" -> FuelIcon
                            "yield" -> YieldIcon
                            else -> Color.Gray
                        }
                        drawArc(
                            color = barColor,
                            startAngle = startAngle,
                            sweepAngle = angles[index],
                            useCenter = false,
                            style = Stroke(width = 50f)
                        )
                        startAngle += angles[index]
                    }
                }
                Text(
                    text = "$total\nTotal",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                data.entries.chunked(2).forEachIndexed { _, chunk ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        chunk.forEachIndexed { _, entry ->
                            val barColor = when (entry.key.lowercase()) {
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
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(barColor, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${entry.key} (${entry.value})")
                            }
                        }
                        // Add spacer if only one item in chunk to maintain layout
                        if (chunk.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
