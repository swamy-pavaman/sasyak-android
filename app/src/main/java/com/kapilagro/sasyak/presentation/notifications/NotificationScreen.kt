package com.kapilagro.sasyak.presentation.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.presentation.common.components.NotificationItem
import com.kapilagro.sasyak.presentation.common.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotificationScreen(
    onTaskClick: (Int) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notificationsState by viewModel.notificationsState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadNotifications()
        }
    )

    LaunchedEffect(notificationsState) {
        if (notificationsState !is NotificationViewModel.NotificationsState.Loading) {
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    // Only show mark all as read if there are unread notifications
                    if (notificationsState is NotificationViewModel.NotificationsState.Success &&
                        (notificationsState as NotificationViewModel.NotificationsState.Success).notifications.any { !it.isRead }
                    ) {
                        IconButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                            Icon(
                                imageVector = Icons.Filled.MarkEmailRead,
                                contentDescription = "Mark All as Read"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when (notificationsState) {
                is NotificationViewModel.NotificationsState.Success -> {
                    val notifications = (notificationsState as NotificationViewModel.NotificationsState.Success).notifications
                    if (notifications.isEmpty()) {
                        EmptyStateView(
                            title = "No notifications yet",
                            message = "New notifications will appear here",
                            icon = TODO(),
                            modifier = TODO()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notifications) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onClick = {
                                        // Mark as read when clicked
                                        viewModel.markNotificationAsRead(notification.id)

                                        // If notification has taskId, navigate to task detail
                                        notification.taskId?.let { taskId ->
                                            onTaskClick(taskId)
                                        }
                                    }
                                )
                            }

                            // Add bottom spacing
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
                is NotificationViewModel.NotificationsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is NotificationViewModel.NotificationsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Failed to load notifications",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadNotifications() }) {
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