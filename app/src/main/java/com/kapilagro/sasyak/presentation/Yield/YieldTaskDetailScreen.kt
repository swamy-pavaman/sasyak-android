package com.kapilagro.sasyak.presentation.yield

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.domain.models.YieldDetails
import com.kapilagro.sasyak.presentation.common.components.StatusIndicator
import com.kapilagro.sasyak.presentation.common.components.TaskTypeChip
import com.kapilagro.sasyak.presentation.common.theme.YieldContainer
import com.kapilagro.sasyak.presentation.common.theme.YieldIcon
import com.kapilagro.sasyak.presentation.tasks.TaskViewModel
import kotlinx.serialization.json.Json
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YieldTaskDetailScreen(
    taskId: Int,
    onBackClick: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel() // Reuse the TaskViewModel
) {
    val taskDetailState by viewModel.taskDetailState.collectAsState()
    val updateTaskState by viewModel.updateTaskState.collectAsState()

    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }

    // Load task detail
    LaunchedEffect(taskId) {
        viewModel.loadTaskDetail(taskId)
    }

    // Handle update success
    LaunchedEffect(updateTaskState) {
        if (updateTaskState is TaskViewModel.UpdateTaskState.Success) {
            viewModel.clearUpdateTaskState()
            viewModel.loadTaskDetail(taskId) // Refresh the task data
        }
    }

    // Approval/Rejection Dialogs
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Approve Yield Report") },
            text = {
                Column {
                    Text("Are you sure you want to approve this yield report?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Comment (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateTaskStatus(taskId,
                            comment.takeIf { it.isNotBlank() }.toString()
                        )
                        showApproveDialog = false
                        comment = ""
                    }
                ) {
                    Text("Approve")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showApproveDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Yield Report") },
            text = {
                Column {
                    Text("Are you sure you want to reject this yield report?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Reason (Required)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (comment.isNotBlank()) {
                            viewModel.updateTaskStatus(taskId, comment)
                            showRejectDialog = false
                            comment = ""
                        }
                    },
                    enabled = comment.isNotBlank()
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRejectDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yield Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YieldIcon,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when (taskDetailState) {
            is TaskViewModel.TaskDetailState.Success -> {
                val taskDetail = (taskDetailState as TaskViewModel.TaskDetailState.Success)
                val task = taskDetail.task
                val advices = taskDetail.advices

                // Parse yield details from JSON
                val yieldDetails = task.detailsJson?.let {
                    try {
                        Json.decodeFromString<YieldDetails>(it)
                    } catch (e: Exception) {
                        null
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Task header with status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Yield Report",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        StatusIndicator(status = task.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Task type
                    TaskTypeChip(taskType = task.taskType)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Task description
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Yield details section
                    if (yieldDetails != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = YieldContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Harvest Details",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = YieldIcon
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Harvest date
                                DetailRow(
                                    icon = Icons.Outlined.CalendarToday,
                                    label = "Date",
                                    value = yieldDetails.harvestDate
                                )

                                // Crop name
                                DetailRow(
                                    icon = Icons.Outlined.Grass,
                                    label = "Crop",
                                    value = yieldDetails.cropName
                                )

                                // Row
                                DetailRow(
                                    icon = Icons.Outlined.LocationOn,
                                    label = "Row",
                                    value = yieldDetails.row.toString()
                                )

                                // Field area
                                if (!yieldDetails.fieldArea.isNullOrEmpty()) {
                                    DetailRow(
                                        icon = Icons.Outlined.CropSquare,
                                        label = "Field Area",
                                        value = "${yieldDetails.fieldArea} acres"
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                Text(
                                    text = "Yield Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Yield quantity and unit
                                DetailRow(
                                    label = "Yield",
                                    value = "${yieldDetails.yieldQuantity} ${yieldDetails.yieldUnit}",
                                    icon = TODO(),
                                    valueColor = TODO()
                                )

                                // Quality grade
                                if (!yieldDetails.qualityGrade.isNullOrEmpty()) {
                                    DetailRow(
                                        icon = Icons.Outlined.Grade,
                                        label = "Quality",
                                        value = yieldDetails.qualityGrade
                                    )
                                }

                                // Moisture content
                                if (!yieldDetails.moistureContent.isNullOrEmpty()) {
                                    DetailRow(
                                        icon = Icons.Outlined.Opacity,
                                        label = "Moisture",
                                        value = "${yieldDetails.moistureContent}%"
                                    )
                                }

                                // Harvest method
                                if (!yieldDetails.harvestMethod.isNullOrEmpty()) {
                                    DetailRow(
                                        icon = Icons.Outlined.Agriculture,
                                        label = "Method",
                                        value = yieldDetails.harvestMethod
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Task metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Created by",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = task.createdBy,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Created on",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = task.createdAt ?: "created",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (task.assignedTo != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Assigned to",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = task.assignedTo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Task actions based on status
                    if (task.status.equals("pending", ignoreCase = true)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { showApproveDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Approve")
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = { showRejectDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reject")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Task advice section if there are any
                    if (advices.isNotEmpty()) {
                        Divider()

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Manager Advice",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column {
                            advices.forEach { advice ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = advice.managerName?:"managername"                                                ,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Text(
                                                text = advice.createdAt?:"createdAt",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = advice.adviceText,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is TaskViewModel.TaskDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = YieldIcon)
                }
            }
            is TaskViewModel.TaskDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Failed to load task details",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.loadTaskDetail(taskId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YieldIcon
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = YieldIcon,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}