package com.kapilagro.sasyak.presentation.tasks

import android.os.Build
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

                    // Task-specific details section
                    TaskDetailsSection(task)

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
                            if (task.status.equals("submitted", ignoreCase = true)) {
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
                            AdviceItem(advice = advice)
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

// TaskAdvice data class definition for use in this file
data class TaskAdvice(
    val id: Int,
    val taskId: Int,
    val adviceText: String,
    val createdBy: String,
    val createdAt: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdviceItem(advice: TaskAdvice) {
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
                    text = advice.adviceText,
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

@Composable
fun TaskDetailsSection(task: Task) {
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

            // Parse the detailsJson
            val details = try {
                task.detailsJson?.let { JSONObject(it) }
            } catch (e: Exception) {
                null
            }

            when (task.taskType.uppercase()) {
                "SCOUTING" -> {
                    // Display scouting-specific fields
                    details?.let {
                        DetailItem("Row", it.optString("Row", ""))
                        DetailItem("Tree No", it.optString("Tree No", ""))
                        DetailItem("Crop Name", it.optString("Crop Name", ""))
                        DetailItem("Scouting Date", it.optString("Scouting Date", ""))
                        DetailItem("Name of Disease", it.optString("Name of Disease", ""))
                        DetailItem("Number of Fruits Seen", it.optString("Number of Fruits Seen", ""))
                        DetailItem("Number of Flowers Seen", it.optString("Number of Flowers Seen", ""))
                        DetailItem("Number of Fruits Dropped", it.optString("Number of Fruits Dropped", ""))
                    }
                }
                "IRRIGATION" -> {
                    // Display irrigation-specific fields
                    details?.let {
                        DetailItem("Block", it.optString("Block", ""))
                        DetailItem("Row", it.optString("Row", ""))
                        DetailItem("Water Source", it.optString("Water Source", ""))
                        DetailItem("Duration (hours)", it.optString("Duration", ""))
                        DetailItem("Water Volume (L)", it.optString("Water Volume", ""))
                    }
                }
                "HARVESTING" -> {
                    // Display harvesting-specific fields
                    details?.let {
                        DetailItem("Block", it.optString("Block", ""))
                        DetailItem("Crop", it.optString("Crop", ""))
                        DetailItem("Quantity (kg)", it.optString("Quantity", ""))
                        DetailItem("Quality Grade", it.optString("Quality Grade", ""))
                        DetailItem("Harvest Date", it.optString("Harvest Date", ""))
                    }
                }
                "SPRAYING" -> {
                    // Display spraying-specific fields
                    details?.let {
                        DetailItem("Block", it.optString("Block", ""))
                        DetailItem("Chemical Name", it.optString("Chemical Name", ""))
                        DetailItem("Application Rate", it.optString("Application Rate", ""))
                        DetailItem("Target Pest/Disease", it.optString("Target", ""))
                        DetailItem("Area Covered", it.optString("Area Covered", ""))
                        DetailItem("Weather Conditions", it.optString("Weather", ""))
                    }
                }
                "FERTILIZATION" -> {
                    // Display fertilization-specific fields
                    details?.let {
                        DetailItem("Block", it.optString("Block", ""))
                        DetailItem("Fertilizer Name", it.optString("Fertilizer Name", ""))
                        DetailItem("Application Rate", it.optString("Application Rate", ""))
                        DetailItem("Method", it.optString("Method", ""))
                        DetailItem("Area Covered", it.optString("Area Covered", ""))
                        DetailItem("Soil Condition", it.optString("Soil Condition", ""))
                    }
                }
                "PRUNING" -> {
                    // Display pruning-specific fields
                    details?.let {
                        DetailItem("Block", it.optString("Block", ""))
                        DetailItem("Row", it.optString("Row", ""))
                        DetailItem("Tree Count", it.optString("Tree Count", ""))
                        DetailItem("Pruning Type", it.optString("Pruning Type", ""))
                        DetailItem("Pruning Intensity", it.optString("Pruning Intensity", ""))
                    }
                }
                "PLANTING" -> {
                    // Display planting-specific fields
                    details?.let {
                        DetailItem("Block", it.optString("Block", ""))
                        DetailItem("Row", it.optString("Row", ""))
                        DetailItem("Crop", it.optString("Crop", ""))
                        DetailItem("Variety", it.optString("Variety", ""))
                        DetailItem("Number of Plants", it.optString("Number of Plants", ""))
                        DetailItem("Spacing", it.optString("Spacing", ""))
                    }
                }
                else -> {
                    // Generic details display for any other type
                    details?.let {
                        val keys = it.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            DetailItem(key, it.optString(key, ""))
                        }
                    }
                }
            }

            // Display images if any
            val images = try {
                task.imagesJson?.let { json ->
                    val jsonArray = JSONArray(json)
                    List(jsonArray.length()) { i -> jsonArray.getString(i) }
                }
            } catch (e: Exception) {
                null
            }

            if (images != null && images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Images",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Display image count as a placeholder
                Text(
                    text = "${images.size} image(s) attached",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Here you could implement the actual image display
                // For example, a LazyRow of images with clickable behavior to open full screen

                /* Example of image display implementation:
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images.size) { index ->
                        // Image component would go here
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                */
            }

            // Display location if available
            details?.optString("Location")?.let { location ->
                if (location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Display location information
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Here you could add a map preview if needed
                }
            }

            // Display any additional notes if present
            details?.optString("Notes")?.let { notes ->
                if (notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Additional Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Helper function to format date time
@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateTime(dateTime: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dt = LocalDateTime.parse(dateTime, formatter)
        val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        dt.format(outputFormatter)
    } catch (e: Exception) {
        dateTime // Return original string if parsing fails
    }
}

// Helper function to parse implementation JSON
private fun getImplementationFromJson(implementationJson: String?): String? {
    return try {
        implementationJson?.let {
            val jsonObject = JSONObject(it)
            jsonObject.optString("text", null)
        }
    } catch (e: Exception) {
        null
    }
}