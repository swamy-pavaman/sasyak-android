package com.kapilagro.sasyak.presentation.tasks

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.presentation.common.components.StatusIndicator
import com.kapilagro.sasyak.presentation.common.components.TaskTypeChip
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Int,
    onBackClick: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val taskDetailState by viewModel.taskDetailState.collectAsState()
    val updateTaskState by viewModel.updateTaskState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }
    var implementationInput by remember { mutableStateOf("") }

    // Load task detail
    LaunchedEffect(taskId) {
        viewModel.loadTaskDetail(taskId)
        viewModel.getCurrentUserRole()
    }

    // Handle update success
    LaunchedEffect(updateTaskState) {
        if (updateTaskState is TaskViewModel.UpdateTaskState.Success) {
            viewModel.clearUpdateTaskState()
            viewModel.loadTaskDetail(taskId) // Refresh data
        }
    }

    // Approve/Reject Dialog
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Approve Task") },
            text = {
                Column {
                    Text("Are you sure you want to approve this task?")
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
                        viewModel.updateTaskStatus(taskId, "approved", comment.takeIf { it.isNotBlank() })
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
            title = { Text("Reject Task") },
            text = {
                Column {
                    Text("Are you sure you want to reject this task?")
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
                            viewModel.updateTaskStatus(taskId, "rejected", comment)
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
                    title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (taskDetailState) {
            is TaskViewModel.TaskDetailState.Success -> {
                val taskDetail = (taskDetailState as TaskViewModel.TaskDetailState.Success)
                val task = taskDetail.task
                val advices = taskDetail.advices

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Task header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Task #${task.id}",
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
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Task-specific details section - SIMPLIFIED VERSION
                    SimplifiedTaskDetailsSection(task)

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

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
                                text = formatDateTime(task.createdAt ?: ""),
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

                    // Display role-based actions
                    when (userRole) {
                        "MANAGER" -> {
                            // Manager can approve/reject tasks and provide advice
                            if (task.status.equals("submitted", ignoreCase = true)|| task.status.equals("pending", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(16.dp))

                                // Using our new TaskActionButtons component
                                com.kapilagro.sasyak.presentation.tasks.components.TaskActionButtons(
                                    onApproveClick = { showApproveDialog = true },
                                    onRejectClick = { showRejectDialog = true }
                                )
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
                        }
                        "SUPERVISOR" -> {
                            // Supervisor can implement approved tasks
                            if (task.status.equals("approved", ignoreCase = true) && getImplementationFromJson(task.implementationJson).isNullOrEmpty()) {
                                Column {
                                    OutlinedTextField(
                                        value = implementationInput,
                                        onValueChange = { implementationInput = it },
                                        label = { Text("Implementation Details") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        maxLines = 5
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (implementationInput.isNotBlank()) {
                                                viewModel.addTaskImplementation(taskId, implementationInput)
                                                implementationInput = ""
                                            }
                                        },
                                        enabled = implementationInput.isNotBlank(),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PlayArrow,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Submit Implementation")
                                    }
                                }
                            }
                        }
                        else -> {
                            // Other users just view the task
                        }
                    }

                    // Display implementation if exists
                    val implementation = getImplementationFromJson(task.implementationJson)
                    if (!implementation.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Implementation",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = implementation,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Task advice section
                    Text(
                        text = "Advice",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (advices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No advice provided for this task yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        advices.forEach { advice ->
                            SimpleAdviceItem(advice = advice)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Add advice input for managers
                    if (userRole == "MANAGER") {
                        Spacer(modifier = Modifier.height(16.dp))

                        var newAdvice by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = newAdvice,
                            onValueChange = { newAdvice = it },
                            label = { Text("Add Advice") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (newAdvice.isNotBlank()) {
                                    viewModel.addTaskAdvice(taskId, newAdvice)
                                    newAdvice = ""
                                }
                            },
                            enabled = newAdvice.isNotBlank(),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Advice")
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
                    CircularProgressIndicator()
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
                        Button(onClick = { viewModel.loadTaskDetail(taskId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Simplified advice item that works with the API format
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SimpleAdviceItem(advice: TaskAdvice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = advice.managerName ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDateTime(advice.createdAt),
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

// Extremely simplified details section with clear visualization
@Composable
fun SimplifiedTaskDetailsSection(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Task Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Direct display of raw JSON for debugging
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2F5))
            ) {
                Column(Modifier.padding(8.dp)) {
                    Text(
                        text = "Raw JSON Data",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = task.detailsJson ?: "No details available",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manually parse and display fields from the task
            if (task.taskType.uppercase() == "SCOUTING") {
                DisplayScoutingFields(task.detailsJson)
            } else {
                Text(
                    text = "Details for ${task.taskType}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Generic field display
                val details = try {
                    task.detailsJson?.let { JSONObject(it) }
                } catch (e: Exception) {
                    null
                }

                if (details != null) {
                    val keys = details.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = details.optString(key, "")
                        DetailItem(key.replaceFirstChar { it.uppercase() }, value)
                    }
                } else {
                    Text("No details available")
                }
            }
        }
    }
}

// Special function just for scouting fields with direct display
@Composable
fun DisplayScoutingFields(detailsJson: String?) {
    Text(
        text = "Scouting Details",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    val json = try {
        detailsJson?.let { JSONObject(it) }
    } catch (e: Exception) {
        Log.e("ScoutingFields", "Error parsing JSON: ${e.message}")
        null
    }

    json?.let {
        DetailItem("Row", it.optString("row", ""))
        DetailItem("Tree Number", it.optString("treeNo", ""))
        DetailItem("Crop Name", it.optString("cropName", ""))
        DetailItem("Scouting Date", it.optString("scoutingDate", ""))
        DetailItem("Disease Name", it.optString("nameOfDisease", ""))
        DetailItem("Fruits Seen", it.optString("noOfFruitSeen", ""))
        DetailItem("Flowers Seen", it.optString("noOfFlowersSeen", ""))
        DetailItem("Fruits Dropped", it.optString("noOfFruitsDropped", ""))
    } ?: Text(
        text = "Error loading scouting details",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

// Helper function to format date time - SIMPLIFIED
@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateTime(dateTime: String): String {
    return try {
        // Handle different date formats safely
        val dt = if (dateTime.contains(".")) {
            val trimmed = dateTime.split(".")[0]
            LocalDateTime.parse(trimmed)
        } else {
            LocalDateTime.parse(dateTime)
        }

        dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    } catch (e: Exception) {
        dateTime // Return original if parsing fails
    }
}

// Helper function to parse implementation JSON - SIMPLIFIED
private fun getImplementationFromJson(implementationJson: String?): String? {
    if (implementationJson == null || implementationJson == "{}" || implementationJson.isEmpty()) {
        return null
    }

    return try {
        JSONObject(implementationJson).optString("text")
    } catch (e: Exception) {
        null
    }
}