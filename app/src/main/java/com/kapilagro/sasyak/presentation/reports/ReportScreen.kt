package com.kapilagro.sasyak.presentation.reports

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.R
import com.kapilagro.sasyak.domain.models.DailyTaskCount
import com.kapilagro.sasyak.presentation.common.theme.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val reportState by viewModel.reportState.collectAsState()
    val chartTab by viewModel.chartTab.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                actions = {
                    IconButton(onClick = { /* Export reports */ }) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(
                                id = R.drawable.ic_drop
                            ),
                            contentDescription = "Export"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Task Reports",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedButton(
                    onClick = { /* Show date picker */ },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("This Week")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            id = R.drawable.ic_calender
                        ),
                        contentDescription = "Select Date",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            TabRow(
                selectedTabIndex = if (chartTab == ReportViewModel.ChartTab.WEEKLY) 0 else 1,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = chartTab == ReportViewModel.ChartTab.WEEKLY,
                    onClick = { viewModel.setChartTab(ReportViewModel.ChartTab.WEEKLY) },
                    text = { Text("Weekly") }
                )
                Tab(
                    selected = chartTab == ReportViewModel.ChartTab.MONTHLY,
                    onClick = { viewModel.setChartTab(ReportViewModel.ChartTab.MONTHLY) },
                    text = { Text("Monthly") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (reportState) {
                is ReportViewModel.ReportState.Success -> {
                    val taskCounts = if (chartTab == ReportViewModel.ChartTab.WEEKLY) {
                        viewModel.getWeeklyTaskCounts()
                    } else {
                        viewModel.getMonthlyTaskCounts()
                    }

                    TaskCompletionChart(
                        taskCounts = taskCounts,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Task Summary",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val report = (reportState as ReportViewModel.ReportState.Success).report
                    TaskTypeSummary(report.tasksByType)
                }
                is ReportViewModel.ReportState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ReportViewModel.ReportState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Failed to load report data",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadTaskReport() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

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

@Composable
fun TaskCompletionChart(
    taskCounts: List<DailyTaskCount>,
    modifier: Modifier = Modifier
) {
    val maxCount = taskCounts.maxOfOrNull { it.count } ?: 0
    val chartHeight = 180.dp

    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .height(chartHeight)
                .align(Alignment.CenterStart),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 8 downTo 0 step 2) {
                Text(
                    text = "$i",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                if (i > 0) Spacer(modifier = Modifier.weight(1f))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(start = 24.dp, top = 8.dp, bottom = 24.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val barWidth = (size.width - 16.dp.toPx()) / taskCounts.size
                val maxHeight = size.height

                for (i in 0..4) {
                    val y = maxHeight - (maxHeight * i / 4)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                taskCounts.forEachIndexed { index, dailyCount ->
                    val barHeight = if (maxCount > 0) {
                        (dailyCount.count.toFloat() / maxCount) * maxHeight
                    } else {
                        0f
                    }

                    val barX = (index * barWidth) + 8.dp.toPx()
                    val barY = maxHeight - barHeight

                    drawRoundRect(
                        color = Info,
                        topLeft = Offset(barX, barY),
                        size = Size(barWidth - 16.dp.toPx(), barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                taskCounts.forEach { dailyCount ->
                    Text(
                        text = dailyCount.days,
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