package com.kapilagro.sasyak.presentation.reports

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kapilagro.sasyak.R
import com.kapilagro.sasyak.presentation.common.theme.*
import com.kapilagro.sasyak.presentation.reports.components.TaskCompletionChart
import com.kapilagro.sasyak.presentation.reports.components.TaskTypeSummary
import com.kapilagro.sasyak.presentation.reports.components.TasksByUser
import com.kapilagro.sasyak.presentation.reports.components.TaskByAvgCompletionTime
import com.kapilagro.sasyak.presentation.reports.components.TaskByStatus

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val reportState by viewModel.reportState.collectAsState()
    val chartTab by viewModel.chartTab.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
//                actions = {
//                    IconButton(onClick = { /* Export reports */ }) {
//                        Icon(
//                            painter = androidx.compose.ui.res.painterResource(
//                                id = R.drawable.ic_drop
//                            ),
//                            contentDescription = "Export"
//                        )
//                    }
//                }
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
                    text = { Text("Daily") }
                )
                Tab(
                    selected = chartTab == ReportViewModel.ChartTab.MONTHLY,
                    onClick = { viewModel.setChartTab(ReportViewModel.ChartTab.MONTHLY) },
                    text = { Text("Weekly") }
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
                    Spacer(modifier = Modifier.height(16.dp))
                    TaskByAvgCompletionTime(report.avgCompletionTimeByType)
                    Spacer(modifier = Modifier.height(16.dp))
                    TaskByStatus(report.tasksByStatus)
                    Spacer(modifier = Modifier.height(16.dp))
                    TasksByUser(report.tasksByUser)
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
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_internet))
                    val lottieAnimatable = rememberLottieAnimatable()
                    LaunchedEffect(composition) {
                        if (composition != null) {
                            lottieAnimatable.animate(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                speed = 1f
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { lottieAnimatable.progress },
                            modifier = Modifier.size(300.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = { viewModel.loadTaskReport() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}