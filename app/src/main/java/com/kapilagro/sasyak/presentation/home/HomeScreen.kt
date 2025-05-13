package com.kapilagro.sasyak.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

import com.kapilagro.sasyak.presentation.common.components.TaskCard
import com.kapilagro.sasyak.presentation.common.components.WeatherCard
import com.kapilagro.sasyak.presentation.common.theme.*
import com.kapilagro.sasyak.presentation.home.components.QuickActionButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onTaskClick: (String) -> Unit,
    onCreateTaskClick: () -> Unit,
    onScannerClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onWeatherDetailsClick: () -> Unit,
    onScoutingTaskClick: () -> Unit,
    onFuelRequestClick: ()-> Unit,
    onSowingTaskClick: () -> Unit,
    onSprayingTaskClick: () -> Unit,
    onYieldTaskClick: () -> Unit,
    onTeamClick:()->Unit,


    onReportsClick: () -> Unit,    // Added for Reports navigation
    onAdviceClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val userState by viewModel.userState.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val tasksState by viewModel.tasksState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    // Permission state for location
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val userName = when (userState) {
        is HomeViewModel.UserState.Success -> (userState as HomeViewModel.UserState.Success).user.name
        else -> ""
    }

    val formattedDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    }

    // Check permissions on first composition and when permission state changes
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            viewModel.loadWeatherData()
        }
    }

    // Request permissions if not granted
    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

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
                    IconButton(onClick = onNotificationClick, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Weather Section with permission handling
            if (!locationPermissionState.allPermissionsGranted) {
                // Show permission request card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Location permission is required to show weather information",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                locationPermissionState.launchMultiplePermissionRequest()
                            }
                        ) {
                            Text("Grant Permission")
                        }
                        if (locationPermissionState.shouldShowRationale) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Location access is needed to provide accurate weather information for your area",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Show weather content when permissions are granted
                when (weatherState) {
                    is HomeViewModel.WeatherState.Success -> {
                        WeatherCard(
                            weatherInfo = (weatherState as HomeViewModel.WeatherState.Success).weatherInfo,
                            onFullDetailsClick = onWeatherDetailsClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp, vertical = 8.dp)
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
                                Text(
                                    text = (weatherState as HomeViewModel.WeatherState.Error).message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadWeatherData() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Role-specific sections
            when (userRole) {
                "MANAGER" -> {
                    ManagerHomeContent(
                        onTaskClick = onTaskClick,
                        tasksState = tasksState,
                        loadTasksData = { viewModel.loadTasksData() },
                        onTeamClick=onTeamClick,
                        onReportsClick = onReportsClick,
                        onAdviceClick = onAdviceClick     // Pass the new callbacks

                    )
                }
                "SUPERVISOR" -> {
                    SupervisorHomeContent(
                        onTaskClick = onTaskClick,
                        tasksState = tasksState,
                        loadTasksData = { viewModel.loadTasksData() },
                        onScoutingTaskClick = onScoutingTaskClick,
                        onFuelRequestClick = onFuelRequestClick,
                        onSowingTaskClick = onSowingTaskClick,
                        onSprayingTaskClick = onSprayingTaskClick,
                        onYieldTaskClick = onYieldTaskClick
                    )
                }
                else -> {
                    // Default view if role is unknown
                    DefaultHomeContent(
                        onTaskClick = onTaskClick,
                        tasksState = tasksState,
                        loadTasksData = { viewModel.loadTasksData() },
                        onSowingTaskClick = onSowingTaskClick,
                        onSprayingTaskClick = onSprayingTaskClick,
                        onYieldTaskClick = onYieldTaskClick
                    )
                }
            }
        }
    }
}

@Composable
fun ManagerHomeContent(
    onTaskClick: (String) -> Unit,
    tasksState: HomeViewModel.TasksState,
    loadTasksData: () -> Unit,
    onTeamClick: ()-> Unit,
    onReportsClick: () -> Unit,  // Added for Reports navigation
    onAdviceClick: () -> Unit,   // Added for Advice navigation
) {
    // Manager-specific quick actions
    Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Outlined.PeopleAlt,
            label = "Team",
            backgroundColor = TeamIcon,
            containerColor = TeamContainer,
            onClick = onTeamClick
        )

        QuickActionButton(
            icon = Icons.Outlined.Assessment,
            label = "Reports",
            backgroundColor = ReportsIcon,
            containerColor = ReportsContainer,
            onClick = onReportsClick
        )

//        QuickActionButton(
//            icon = Icons.AutoMirrored.Outlined.Assignment,
//            label = "Tasks",
//            backgroundColor = TasksIcon,
//            containerColor = TasksContainer,
//            onClick = { /* Handle tasks */ }
//        )

        QuickActionButton(
            icon = Icons.Outlined.Healing,
            label = "Advice",
            backgroundColor = AdviceIcon,
            containerColor = AdviceContainer,
            onClick = onAdviceClick
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // UPDATED: Manager Task Sections with New and My Tabs
    var selectedManagerTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Tasks",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        TabRow(
            selectedTabIndex = selectedManagerTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedManagerTab == 0,
                onClick = { selectedManagerTab = 0 },
                text = { Text("New") }
            )
            Tab(
                selected = selectedManagerTab == 1,
                onClick = { selectedManagerTab = 1 },
                text = { Text("My") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedManagerTab) {
            0 -> {
                // New tasks (created by supervisors)
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
                                Text("No new tasks available", style = MaterialTheme.typography.bodyLarge)
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
                                Button(onClick = loadTasksData) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // My tasks (created by manager)
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
                                Text("No tasks created by you", style = MaterialTheme.typography.bodyLarge)
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
                                Button(onClick = loadTasksData) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupervisorHomeContent(
    onTaskClick: (String) -> Unit,
    tasksState: HomeViewModel.TasksState,
    loadTasksData: () -> Unit,
    onScoutingTaskClick: () -> Unit,
    onFuelRequestClick: () -> Unit,
    onSowingTaskClick: () -> Unit,
    onSprayingTaskClick: () -> Unit,
    onYieldTaskClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Supervisor-specific quick actions
    Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        item {
            QuickActionButton(
                icon = Icons.Outlined.Search,
                label = "Scouting",
                backgroundColor = ScoutingIcon,
                containerColor = ScoutingContainer,
                onClick = onScoutingTaskClick
            )
        }

        item {
            QuickActionButton(
                icon = Icons.Outlined.Opacity,
                label = "Spraying",
                backgroundColor = SprayingIcon,
                containerColor = SprayingContainer,
                onClick = onSprayingTaskClick
            )
        }

        item {
            QuickActionButton(
                icon = Icons.Outlined.Grass,
                label = "Sowing",
                backgroundColor = SowingIcon,
                containerColor = SowingContainer,
                onClick = onSowingTaskClick
            )
        }

        item {
            QuickActionButton(
                icon = Icons.Outlined.LocalGasStation,
                label = "Fuel",
                backgroundColor = FuelIcon,
                containerColor = FuelContainer,
                onClick = onFuelRequestClick
            )
        }

        item {
            QuickActionButton(
                icon = Icons.Outlined.Balance,
                label = "Yield",
                backgroundColor = YieldIcon,
                containerColor = YieldContainer,
                onClick = onYieldTaskClick
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // UPDATED: Supervisor Task Sections with Status Tabs
    var selectedTaskTab by remember { mutableStateOf(0) }

    // Load the appropriate tasks when tab changes
    LaunchedEffect(selectedTaskTab) {
        when (selectedTaskTab) {
            0 -> viewModel.loadTasksByStatus("submitted")
            1 -> viewModel.loadTasksByStatus("approved")
            2 -> viewModel.loadTasksByStatus("rejected")
            3 -> viewModel.loadAssignedTasks() // Now calls the correct method
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Tasks",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        SegmentedTaskControl(
            selectedIndex = selectedTaskTab,
            onSegmentSelected = { selectedTaskTab = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Task list based on selected tab
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
                        val statusMessage = when (selectedTaskTab) {
                            0 -> "No submitted tasks"
                            1 -> "No approved tasks"
                            2 -> "No rejected tasks"
                            3 -> "No assigned tasks"
                            else -> "No tasks available"
                        }
                        Text(statusMessage, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    Column {
                        tasks.forEach { task ->
                            TaskCard(
                                task = task,
                                onClick = { onTaskClick(task.id.toString()) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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
                            text = (tasksState as HomeViewModel.TasksState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            // Retry based on current tab
                            when (selectedTaskTab) {
                                0 -> viewModel.loadTasksByStatus("submitted")
                                1 -> viewModel.loadTasksByStatus("approved")
                                2 -> viewModel.loadTasksByStatus("rejected")
                                3 -> viewModel.loadAssignedTasks() // Updated to call loadAssignedTasks
                            }
                        }) {
                            Text("Retry")
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedTaskControl(
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit
) {
    val segments = listOf("Submitted", "Approved", "Rejected", "Assigned")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        segments.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onSegmentSelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}




@Composable
fun DefaultHomeContent(
    onTaskClick: (String) -> Unit,
    tasksState: HomeViewModel.TasksState,
    loadTasksData: () -> Unit,
    onSowingTaskClick: () -> Unit,
    onSprayingTaskClick: () -> Unit,
    onYieldTaskClick: () -> Unit
) {
    // Default quick actions
    Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        QuickActionButton(
            icon = Icons.Outlined.Opacity,
            label = "Spraying",
            backgroundColor = SprayingIcon,
            containerColor = SprayingContainer,
            onClick = onSprayingTaskClick
        )

        QuickActionButton(
            icon = Icons.Outlined.Grass,
            label = "Sowing",
            backgroundColor = SowingIcon,
            containerColor = SowingContainer,
            onClick = onSowingTaskClick
        )

        QuickActionButton(
            icon = Icons.Outlined.LocalGasStation,
            label = "Fuel",
            backgroundColor = FuelIcon,
            containerColor = FuelContainer,
            onClick = { /* Handle fuel action */ }
        )

        QuickActionButton(
            icon = Icons.Outlined.Balance,
            label = "Yield",
            backgroundColor = YieldIcon,
            containerColor = YieldContainer,
            onClick = onYieldTaskClick
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Task section
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

    // Task Cards
    TasksList(tasksState, onTaskClick, loadTasksData)
}

@Composable
fun TasksList(
    tasksState: HomeViewModel.TasksState,
    onTaskClick: (String) -> Unit,
    loadTasksData: () -> Unit
) {
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
                    Text("No tasks available", style = MaterialTheme.typography.bodyLarge)
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
                    Button(onClick = loadTasksData) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
