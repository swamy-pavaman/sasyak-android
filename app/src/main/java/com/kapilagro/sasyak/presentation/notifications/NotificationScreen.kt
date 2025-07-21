package com.kapilagro.sasyak.presentation.notifications

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.presentation.common.components.EmptyStateView
import com.kapilagro.sasyak.presentation.common.theme.UnreadNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotificationScreen(
    onTaskClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val unreadCountState by viewModel.unreadCountState.collectAsState()
    val notificationsState by viewModel.notificationsState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            // Trigger when scrolled to the last 2 items
            lastVisible >= totalItems - 2 && totalItems > 0
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadNotifications()
        }
    )

    // Reset refreshing after loading is complete
    LaunchedEffect(notificationsState) {
        if (notificationsState !is NotificationViewModel.NotificationsState.Loading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !viewModel.isLoading() && viewModel.hasMoreData()) {
            viewModel.loadMoreNotifications()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notificationsState is NotificationViewModel.NotificationsState.Success &&
                        (notificationsState as NotificationViewModel.NotificationsState.Success).notifications.any { !it.isRead }
                    ) {
//                        IconButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
//                            Icon(
//                                imageVector = Icons.Filled.MarkEmailRead,
//                                contentDescription = "Mark All as Read"
//                            )
//                        }
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
            when (val state = notificationsState) {
                is NotificationViewModel.NotificationsState.Success -> {
                    val notifications = state.notifications
                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateView(
                                title = "No notifications yet",
                                message = "New notifications will appear here",
                                icon = Icons.Outlined.NotificationAdd
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notifications) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onClick = {
                                        if (notification.isRead == false) {
                                            viewModel.markNotificationAsRead(notification.id)
                                        }
                                        notification.taskId?.let { onTaskClick(it) }
                                    }
                                )
                            }

                            item {
                                if (viewModel.isLoading()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
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

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    // Light border for read notifications, primary color border for unread notifications
    val cardBorderColor = if (!notification.isRead) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline // More visible border for read notifications
    }

    val backgroundColor = if (!notification.isRead) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(
            width = if (!notification.isRead) 1.dp else 0.8.dp, // Slightly thicker border for both
            color = cardBorderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Unread indicator
                if (!notification.isRead) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(UnreadNotification, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UnreadNotification,
                        )
                    }
                }
                Text(
                    text = notification.title ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (!notification.isRead) FontWeight.Medium else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message ?: "No Message",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (!notification.isRead) FontWeight.Light else FontWeight.ExtraLight,
                    color = if (!notification.isRead)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = calculateTimeAgo(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



fun calculateTimeAgo(createdAt: String): String {
    try {
        // Define the input format (ISO 8601)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC") // createdAt is in UTC
        val createdDate = sdf.parse(createdAt) ?: return "Unknown time"

        // Get current time in IST
        val currentDate = Date()
        val istTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
        val sdfIst = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdfIst.timeZone = istTimeZone
        val currentTimeInIst = sdfIst.parse(sdfIst.format(currentDate)) ?: return "Unknown time"

        // Calculate difference in milliseconds
        val diffInMillis = currentTimeInIst.time - createdDate.time
        val seconds = diffInMillis / 1000

        return when {
            seconds < 60 -> "Just now"
            seconds < 3600 -> "${seconds / 60} minutes ago"
            seconds < 86400 -> "${seconds / 3600} hours ago"
            seconds < 604800 -> "${seconds / 86400} days ago"
            else -> "${seconds / 604800} weeks ago"
        }
    } catch (e: Exception) {
        return "Unknown time"
    }
}