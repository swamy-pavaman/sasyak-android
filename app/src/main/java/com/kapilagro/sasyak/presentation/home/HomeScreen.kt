package com.kapilagro.sasyak.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.presentation.common.components.TaskCard
import com.kapilagro.sasyak.presentation.common.components.WeatherCard
import com.kapilagro.sasyak.presentation.common.theme.*
import com.kapilagro.sasyak.presentation.home.components.QuickActionButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTaskClick: (String) -> Unit, // Adjusted to match task.id type
    onCreateTaskClick: () -> Unit,
    onScannerClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val tasksState by viewModel.tasksState.collectAsState()

    val userName = when (userState) {
        is HomeViewModel.UserState.Success -> (userState as HomeViewModel.UserState.Success).user.name
        else -> ""
    }

    val formattedDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hello, ${userName.split(" ").firstOrNull() ?: ""}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onScannerClick) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Scan Plant"
                        )
                    }
                    IconButton(onClick = { /* TODO: Add search logic */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTaskClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Create Task"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Weather Section
            when (weatherState) {
                is HomeViewModel.WeatherState.Success -> {
                    WeatherCard(
                        weatherInfo = (weatherState as HomeViewModel.WeatherState.Success).weatherInfo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                is HomeViewModel.WeatherState.Loading -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is HomeViewModel.WeatherState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Unable to load weather", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadWeatherData() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            QuickActionsRow()

            Spacer(modifier = Modifier.height(16.dp))

            // Task Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Task List", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { /* TODO: Handle View All */ }) {
                    Text("View All")
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                listOf("Pending", "Approved", "Rejected").forEachIndexed { index, title ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task Cards
            when (tasksState) {
                is HomeViewModel.TasksState.Success -> {
                    val tasks = (tasksState as HomeViewModel.TasksState.Success).tasks
                    if (tasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No pending tasks", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        tasks.forEach { task ->
                            TaskCard(
                                task = task,
                                onClick = { onTaskClick(task.id.toString()) }
                            )
                        }
                    }
                }
                is HomeViewModel.TasksState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is HomeViewModel.TasksState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Failed to load tasks",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadTasksData() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QuickActionsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        QuickActionButton(
            icon = Icons.Outlined.Search,
            label = "Scouting",
            backgroundColor = Blue500,
            onClick = { /* Handle scouting action */ }
        )

        QuickActionButton(
            icon = Icons.Outlined.Opacity,
            label = "Spraying",
            backgroundColor = Green500,
            onClick = { /* Handle spraying action */ }
        )

        QuickActionButton(
            icon = Icons.Outlined.Grass,
            label = "Sowing",
            backgroundColor = Purple500,
            onClick = { /* Handle sowing action */ }
        )

        QuickActionButton(
            icon = Icons.Outlined.LocalGasStation,
            label = "Fuel",
            backgroundColor = Orange500,
            onClick = { /* Handle fuel action */ }
        )

        QuickActionButton(
            icon = Icons.Outlined.Balance,
            label = "Yield",
            backgroundColor = Pink500,
            onClick = { /* Handle yield action */ }
        )
    }
}
