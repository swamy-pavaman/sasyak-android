package com.kapilagro.sasyak.presentation.sowing

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kapilagro.sasyak.presentation.common.components.ErrorView
import com.kapilagro.sasyak.presentation.common.components.TaskCard
import com.kapilagro.sasyak.presentation.common.filter.FilterViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SowingScreen(
    onTaskCreated: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    sowingViewModel: SowingListViewModel = hiltViewModel(),
    filterViewModel: FilterViewModel = hiltViewModel()
) {
    var selectedTab by rememberSaveable { mutableStateOf("All") }
    val approvedListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val rejectedListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val allListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    val currentListState = when (selectedTab) {
        "Approved" -> approvedListState
        "Rejected" -> rejectedListState
        "All" -> allListState
        else -> allListState
    }

    // Collect states from both ViewModels
    val sowingTasksState by sowingViewModel.tasksState.collectAsState()
    val tasksStateOfApproved by filterViewModel.tasksStateOfApproved.collectAsState()
    val tasksStateOfRejected by filterViewModel.tasksStateOfRejected.collectAsState()
    val isRefreshing by sowingViewModel.refreshing.collectAsState()
    val approvedTaskCount by filterViewModel.ApprovedTaskCount.collectAsState()
    val rejectedTaskCount by filterViewModel.RejectedTaskCount.collectAsState()
    val allTaskCount by sowingViewModel.taskCount.collectAsState()

    // Load tasks based on selected tab
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            "Approved" -> {
                if (tasksStateOfApproved !is FilterViewModel.TasksState.Success) {
                    filterViewModel.pagesToZero("approved")
                    filterViewModel.loadTasksByFilter(status = "approved", taskType = "SOWING", refresh = true)
                }
            }
            "Rejected" -> {
                if (tasksStateOfRejected !is FilterViewModel.TasksState.Success) {
                    filterViewModel.pagesToZero("rejected")
                    filterViewModel.loadTasksByFilter(status = "rejected", taskType = "SOWING", refresh = true)
                }
            }
            "All" -> {
                if (sowingTasksState !is SowingListViewModel.TasksState.Success) {
                    sowingViewModel.loadSowingTasks(refresh = true)
                }
            }
        }
    }

    // Handle pagination for both ViewModels
    LaunchedEffect(currentListState) {
        snapshotFlow {
            val layoutInfo = currentListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) false else {
                val lastVisibleItem = visibleItemsInfo.last()
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 2
            }
        }
            .distinctUntilChanged()
            .collect { isAtBottom ->
                if (isAtBottom) {
                    when (selectedTab) {
                        "Approved" -> {
                            if (tasksStateOfApproved is FilterViewModel.TasksState.Success &&
                                !(tasksStateOfApproved as FilterViewModel.TasksState.Success).isLastPage
                            ) {
                                filterViewModel.loadMoreTasksOfApproved()
                            }
                        }
                        "Rejected" -> {
                            if (tasksStateOfRejected is FilterViewModel.TasksState.Success &&
                                !(tasksStateOfRejected as FilterViewModel.TasksState.Success).isLastPage
                            ) {
                                filterViewModel.loadMoreTasksOfRejected()
                            }
                        }
                        "All" -> {
                            if (sowingTasksState is SowingListViewModel.TasksState.Success &&
                                !(sowingTasksState as SowingListViewModel.TasksState.Success).isLastPage
                            ) {
                                sowingViewModel.loadMoreTasks()
                            }
                        }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sowing") },
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
            ExtendedFloatingActionButton(
                onClick = { onTaskCreated() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Sowing"
                    )
                },
                text = {
                    Text("New Sowing")
                }
            )
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
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sowing Tasks",
                    style = MaterialTheme.typography.titleLarge
                )

                // Show task count based on selected tab
                when (selectedTab) {
                    "Approved" -> {
                        if (tasksStateOfApproved is FilterViewModel.TasksState.Success) {
                            val taskCount = approvedTaskCount
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
                    "Rejected" -> {
                        if (tasksStateOfRejected is FilterViewModel.TasksState.Success) {
                            val taskCount = rejectedTaskCount
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
                    "All" -> {
                        if (sowingTasksState is SowingListViewModel.TasksState.Success) {
                            val taskCount = allTaskCount
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
                }
            }

            SegmentedTaskControl(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = {
                    when (selectedTab) {
                        "Approved" -> filterViewModel.refreshTasks(status ="approved", taskType = "SOWING")
                        "Rejected" -> filterViewModel.refreshTasks(status="rejected",taskType = "SOWING")
                        "All" -> sowingViewModel.refreshTasks()
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    "Approved" -> {
                        when (tasksStateOfApproved) {
                            is FilterViewModel.TasksState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            is FilterViewModel.TasksState.Success -> {
                                val tasks = (tasksStateOfApproved as FilterViewModel.TasksState.Success).tasks
                                val isLastPage = (tasksStateOfApproved as FilterViewModel.TasksState.Success).isLastPage

                                if (tasks.isEmpty()) {
                                    EmptyStateForTasks(selectedTab)
                                } else {
                                    LazyColumn(
                                        state = approvedListState,
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
                            is FilterViewModel.TasksState.Error -> {
                                ErrorView(
                                    message = (tasksStateOfApproved as FilterViewModel.TasksState.Error).message,
                                    onRetry = { filterViewModel.refreshTasks("approved", taskType = "SOWING") },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    "Rejected" -> {
                        when (tasksStateOfRejected) {
                            is FilterViewModel.TasksState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            is FilterViewModel.TasksState.Success -> {
                                val tasks = (tasksStateOfRejected as FilterViewModel.TasksState.Success).tasks
                                val isLastPage = (tasksStateOfRejected as FilterViewModel.TasksState.Success).isLastPage

                                if (tasks.isEmpty()) {
                                    EmptyStateForTasks(selectedTab)
                                } else {
                                    LazyColumn(
                                        state = rejectedListState,
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
                            is FilterViewModel.TasksState.Error -> {
                                ErrorView(
                                    message = (tasksStateOfRejected as FilterViewModel.TasksState.Error).message,
                                    onRetry = { filterViewModel.refreshTasks("rejected", taskType = "SOWING") },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    "All" -> {
                        when (sowingTasksState) {
                            is SowingListViewModel.TasksState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            is SowingListViewModel.TasksState.Success -> {
                                val tasks = (sowingTasksState as SowingListViewModel.TasksState.Success).tasks
                                val isLastPage = (sowingTasksState as SowingListViewModel.TasksState.Success).isLastPage

                                if (tasks.isEmpty()) {
                                    EmptyStateForTasks(selectedTab)
                                } else {
                                    LazyColumn(
                                        state = allListState,
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
                            is SowingListViewModel.TasksState.Error -> {
                                ErrorView(
                                    message = (sowingTasksState as SowingListViewModel.TasksState.Error).message,
                                    onRetry = { sowingViewModel.refreshTasks() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateForTasks(selectedTab: String) {
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
                "Approved" -> "No approved sowing tasks"
                "Rejected" -> "No rejected sowing tasks"
                else -> "No sowing tasks found"
            }

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create a new sowing task or pull down to refresh",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SegmentedTaskControl(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val segments = listOf("Approved", "Rejected", "All")

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
        segments.forEach { tab ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (selectedTab == tab) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = if (selectedTab == tab) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}