package com.kapilagro.sasyak.presentation.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.presentation.common.components.TaskCard
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.kapilagro.sasyak.presentation.tasks.components.TaskTabRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TaskListScreen(
    onTaskClick: (Int) -> Unit,
    onCreateTaskClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val taskListState by viewModel.taskListState.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(taskListState) {
        if (taskListState !is TaskViewModel.TaskListState.Loading) {
            isRefreshing = false
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadTasks(0, 10)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Task"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Using our custom TaskTabRow instead of the Material3 TabRow
            TaskTabRow(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.onTabSelected(it) },
                pendingCount = (taskListState as? TaskViewModel.TaskListState.Success)?.let {
                    it.tasks.count { task -> task.status.equals("pending", ignoreCase = true) }
                } ?: 0,
                approvedCount = (taskListState as? TaskViewModel.TaskListState.Success)?.let {
                    it.tasks.count { task -> task.status.equals("approved", ignoreCase = true) }
                } ?: 0,
                rejectedCount = (taskListState as? TaskViewModel.TaskListState.Success)?.let {
                    it.tasks.count { task -> task.status.equals("rejected", ignoreCase = true) }
                } ?: 0
            )

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
                                        text = "No ${selectedTab.name.lowercase()} tasks",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = when (selectedTab) {
                                            TaskViewModel.TaskTab.PENDING -> "Tasks waiting for approval will appear here"
                                            TaskViewModel.TaskTab.APPROVED -> "Approved tasks will appear here"
                                            TaskViewModel.TaskTab.REJECTED -> "Rejected tasks will appear here"
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