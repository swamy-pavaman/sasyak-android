package com.kapilagro.sasyak.presentation.reports

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.R
import com.kapilagro.sasyak.presentation.common.theme.*
import com.kapilagro.sasyak.presentation.reports.components.TaskCompletionChart
import com.kapilagro.sasyak.presentation.reports.components.TaskTypeSummary
import com.kapilagro.sasyak.presentation.home.SegmentedTaskControl

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

            SegmentedTaskControl(
                segments = listOf("Weekly", "Monthly"),
                selectedIndex = if (chartTab == ReportViewModel.ChartTab.WEEKLY) 0 else 1,
                onSegmentSelected = { index ->
                    viewModel.setChartTab(
                        if (index == 0) ReportViewModel.ChartTab.WEEKLY
                        else ReportViewModel.ChartTab.MONTHLY
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                backgroundColor = LemonLight // Use LemonLight for background
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (reportState) {
                is ReportViewModel.ReportState.Success -> {
                    val taskCounts = if (chartTab == ReportViewModel.ChartTab.WEEKLY) {
                        viewModel.getWeeklyTaskCounts()
                    } else {
                        viewModel.getMonthlyTaskCounts()
                    }

                    android.util.Log.d(
                        "ReportScreen",
                        "Task Counts for Chart - Completed: ${taskCounts.first.size}, Created: ${taskCounts.second.size}"
                    )

                    TaskCompletionChart(
                        taskCounts = taskCounts,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                            .height(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ReportViewModel.ReportState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
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