package com.kapilagro.sasyak.presentation.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.airbnb.lottie.compose.* // Import Lottie dependencies
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kapilagro.sasyak.R // Import the R class to access the raw resource
import com.kapilagro.sasyak.presentation.common.components.TaskCard
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.*
import com.kapilagro.sasyak.presentation.tasks.components.TabItem
import com.kapilagro.sasyak.presentation.tasks.components.TaskTabRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onTaskClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    navController: NavHostController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val taskListState by viewModel.taskListState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isRefreshing by viewModel.refreshing.collectAsState()
    val listState = rememberLazyListState()

    // Task counts
    val supervisorTaskCount by viewModel.supervisorTaskCount.collectAsState()
    val createdTaskCount by viewModel.createdTaskCount.collectAsState()
    val assignedTaskCount by viewModel.assignedTaskCount.collectAsState()

    // Use the current route as a key to trigger reinitialization
    val currentRoute by navController.currentBackStackEntryAsState()
    val routeKey = currentRoute?.destination?.route

    // Fetch user role and reinitialize when the route changes
    LaunchedEffect(routeKey) {
        viewModel.getCurrentUserRole()
    }

    // Pagination logic
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) false else {
                val lastVisibleItem = visibleItemsInfo.last()
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 2
            }
        }
            .distinctUntilChanged()
            .collect { isAtBottom ->
                if (isAtBottom && taskListState is TaskViewModel.TaskListState.Success &&
                    !(taskListState as TaskViewModel.TaskListState.Success).isLastPage
                ) {
                    viewModel.loadMoreTasks()
                }
            }
    }

    // Define tabs based on user role
    val tabs: List<TabItem<TaskViewModel.TaskTab>> = when (userRole) {
        "MANAGER" -> listOf(
            TabItem(
                id = TaskViewModel.TaskTab.SUPERVISORS,
                title = "Supervisors",
                count = supervisorTaskCount
            ),
            TabItem(
                id = TaskViewModel.TaskTab.ME,
                title = "Me",
                count = createdTaskCount
            )
        )
        "SUPERVISOR" -> listOf(
            TabItem(
                id = TaskViewModel.TaskTab.ASSIGNED,
                title = "Assigned",
                count = assignedTaskCount
            ),
            TabItem(
                id = TaskViewModel.TaskTab.CREATED,
                title = "Created",
                count = createdTaskCount
            )
        )
        else -> emptyList()
    }

    var isFabMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (userRole == "MANAGER") {
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedVisibility(
                        visible = isFabMenuOpen,
                        enter = expandVertically(animationSpec = tween(200)),
                        exit = shrinkVertically(animationSpec = tween(200))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(Screen.ScoutingRequestScreen.route)
                                        isFabMenuOpen = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        navController.navigate(Screen.ScoutingRequestScreen.route)
                                        isFabMenuOpen = false
                                    },
                                    containerColor = ScoutingContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Scouting Task",
                                        tint = ScoutingIcon
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scouting Task", style = MaterialTheme.typography.labelLarge)
                            }
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(Screen.FuelRequestScreen.route)
                                        isFabMenuOpen = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        navController.navigate(Screen.FuelRequestScreen.route)
                                        isFabMenuOpen = false
                                    },
                                    containerColor = FuelContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalGasStation,
                                        contentDescription = "Fuel Task",
                                        tint = FuelIcon
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fuel Task", style = MaterialTheme.typography.labelLarge)
                            }
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(Screen.SprayingRequestScreen.route)
                                        isFabMenuOpen = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        navController.navigate(Screen.SprayingRequestScreen.route)
                                        isFabMenuOpen = false
                                    },
                                    containerColor = SprayingContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Opacity,
                                        contentDescription = "Spray Task",
                                        tint = SprayingIcon
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Spray Task", style = MaterialTheme.typography.labelLarge)
                            }
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(Screen.YieldRequestScreen.route)
                                        isFabMenuOpen = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        navController.navigate(Screen.YieldRequestScreen.route)
                                        isFabMenuOpen = false
                                    },
                                    containerColor = YieldContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Balance,
                                        contentDescription = "Yield Task",
                                        tint = YieldIcon
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Yield Task", style = MaterialTheme.typography.labelLarge)
                            }
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(Screen.SowingRequestScreen.route)
                                        isFabMenuOpen = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        navController.navigate(Screen.SowingRequestScreen.route)
                                        isFabMenuOpen = false
                                    },
                                    containerColor = SowingContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Grass,
                                        contentDescription = "Sowing Task",
                                        tint = SowingIcon
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sowing Task", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                    FloatingActionButton(
                        onClick = { isFabMenuOpen = !isFabMenuOpen },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Task"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (taskListState is TaskViewModel.TaskListState.Success) {
                    val taskCount = when (selectedTab) {
                        TaskViewModel.TaskTab.SUPERVISORS -> supervisorTaskCount
                        TaskViewModel.TaskTab.ME -> createdTaskCount
                        TaskViewModel.TaskTab.ASSIGNED -> assignedTaskCount
                        TaskViewModel.TaskTab.CREATED -> createdTaskCount
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "$taskCount Tasks",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            if (userRole != null && tabs.isNotEmpty()) {
                TaskTabRow(
                    selectedTab = selectedTab,
                    tabs = tabs,
                    onTabSelected = { tab ->
                        viewModel.onTabSelected(tab)
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.refreshTasks() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (taskListState) {
                    is TaskViewModel.TaskListState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is TaskViewModel.TaskListState.Success -> {
                        val tasks = (taskListState as TaskViewModel.TaskListState.Success).tasks
                        val isLastPage = (taskListState as TaskViewModel.TaskListState.Success).isLastPage

                        if (tasks.isEmpty()) {
                            EmptyStateForTasks(selectedTab)
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(tasks) { task ->
                                    TaskCard(
                                        task = task,
                                        onClick = { onTaskClick(task.id) }
                                    )
                                }
                                if (!isLastPage) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(36.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is TaskViewModel.TaskListState.Error -> {
                        ErrorView(
                            message = (taskListState as TaskViewModel.TaskListState.Error).message,
                            onRetry = { viewModel.refreshTasks() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateForTasks(selectedTab: TaskViewModel.TaskTab) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val message = when (selectedTab) {
                TaskViewModel.TaskTab.SUPERVISORS -> "No supervisor tasks"
                TaskViewModel.TaskTab.ME -> "No tasks created by you"
                TaskViewModel.TaskTab.ASSIGNED -> "No assigned tasks"
                TaskViewModel.TaskTab.CREATED -> "No created tasks"
            }

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (selectedTab) {
                    TaskViewModel.TaskTab.SUPERVISORS -> "Tasks from supervisors will appear here"
                    TaskViewModel.TaskTab.ME -> "Tasks created by you will appear here"
                    TaskViewModel.TaskTab.ASSIGNED -> "Assigned tasks will appear here"
                    TaskViewModel.TaskTab.CREATED -> "Tasks you created will appear here"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Load the Lottie animation composition
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_internet))
    val lottieAnimatable = rememberLottieAnimatable()

    // Play the Lottie animation continuously once it's loaded
    LaunchedEffect(composition) {
        if (composition != null) {
            lottieAnimatable.animate(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                speed = 1f
            )
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Just show the Lottie animation
            LottieAnimation(
                composition = composition,
                progress = { lottieAnimatable.progress },
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            /*Text(
                text = message, // use the passed `message` instead of hardcoded one
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp)) */

            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}
