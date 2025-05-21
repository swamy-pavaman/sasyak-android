package com.kapilagro.sasyak.presentation.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.presentation.common.components.TaskCard
import com.kapilagro.sasyak.presentation.common.theme.*
import com.kapilagro.sasyak.presentation.tasks.components.TabItem
import com.kapilagro.sasyak.presentation.tasks.components.TaskTabRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TaskListScreen(
    onTaskClick: (Int) -> Unit,
    onCreateTaskClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val taskListState by viewModel.taskListState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var isFabMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(taskListState) {
        if (taskListState !is TaskViewModel.TaskListState.Loading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getCurrentUserRole()
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadTasks(0, 10)
        }
    )

    // Define tabs based on user role
    val tabs: List<TabItem<TaskViewModel.TaskTab>> = when (userRole) {
        "MANAGER" -> listOf(
            TabItem(
                id = TaskViewModel.TaskTab.SUPERVISORS,
                title = "Supervisors",
                count = (taskListState as? TaskViewModel.TaskListState.Success)?.totalCount ?: 0
            ),
            TabItem(
                id = TaskViewModel.TaskTab.ME,
                title = "Me",
                count = (taskListState as? TaskViewModel.TaskListState.Success)?.totalCount ?: 0
            )
        )
        "SUPERVISOR" -> listOf(
            TabItem(
                id = TaskViewModel.TaskTab.ASSIGNED,
                title = "Assigned",
                count = (taskListState as? TaskViewModel.TaskListState.Success)?.totalCount ?: 0
            ),
            TabItem(
                id = TaskViewModel.TaskTab.BY_STATUS,
                title = "By Status",
                count = (taskListState as? TaskViewModel.TaskListState.Success)?.totalCount ?: 0
            ),
            TabItem(
                id = TaskViewModel.TaskTab.CREATED,
                title = "Created",
                count = (taskListState as? TaskViewModel.TaskListState.Success)?.totalCount ?: 0
            )
        )
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
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
                        FloatingActionButton(
                            onClick = {
                                onCreateTaskClick("scouting")
                                isFabMenuOpen = false
                            },
                            containerColor = ScoutingContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Scouting Task",
                                tint = ScoutingIcon
                            )
                        }
                        FloatingActionButton(
                            onClick = {
                                onCreateTaskClick("fuel")
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
                        FloatingActionButton(
                            onClick = {
                                onCreateTaskClick("spray")
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
                        FloatingActionButton(
                            onClick = {
                                onCreateTaskClick("yield")
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
                        FloatingActionButton(
                            onClick = {
                                onCreateTaskClick("sowing")
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (tabs.isNotEmpty()) {
                TaskTabRow(
                    selectedTab = selectedTab,
                    tabs = tabs,
                    onTabSelected = { viewModel.onTabSelected(it) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when (taskListState) {
                    is TaskViewModel.TaskListState.Success -> {
                        val tasks = (taskListState as TaskViewModel.TaskListState.Success).tasks
                        if (tasks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = when (selectedTab) {
                                            TaskViewModel.TaskTab.SUPERVISORS -> "No supervisor tasks"
                                            TaskViewModel.TaskTab.ME -> "No tasks created by you"
                                            TaskViewModel.TaskTab.ASSIGNED -> "No assigned tasks"
                                            TaskViewModel.TaskTab.BY_STATUS -> "No tasks by status"
                                            TaskViewModel.TaskTab.CREATED -> "No created tasks"
                                            else -> "No tasks"
                                        },
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = when (selectedTab) {
                                            TaskViewModel.TaskTab.SUPERVISORS -> "Tasks from supervisors will appear here"
                                            TaskViewModel.TaskTab.ME -> "Tasks created by you will appear here"
                                            TaskViewModel.TaskTab.ASSIGNED -> "Assigned tasks will appear here"
                                            TaskViewModel.TaskTab.BY_STATUS -> "Tasks filtered by status will appear here"
                                            TaskViewModel.TaskTab.CREATED -> "Tasks you created will appear here"
                                            else -> "Tasks will appear here"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(tasks) { task ->
                                    TaskCard(
                                        task = task,
                                        onClick = { onTaskClick(task.id) }
                                    )
                                }
                            }
                        }
                    }
                    is TaskViewModel.TaskListState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is TaskViewModel.TaskListState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Failed to load tasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadTasks(0, 10) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}