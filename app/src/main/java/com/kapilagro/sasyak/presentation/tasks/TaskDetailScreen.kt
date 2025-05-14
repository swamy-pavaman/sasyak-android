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
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.presentation.common.components.TaskTypeChip
import kotlinx.coroutines.delay
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

    var comment by remember { mutableStateOf("") }
    var implementationInput by remember { mutableStateOf("") }
    var shouldRefresh by remember { mutableStateOf(false) }

    // Load task detail
    LaunchedEffect(taskId, shouldRefresh) {
        viewModel.loadTaskDetail(taskId)
        viewModel.getCurrentUserRole()
        if (shouldRefresh) {
            // Reset the refresh flag after loading
            shouldRefresh = false
        }
    }

    // Handle update success
    LaunchedEffect(updateTaskState) {
        if (updateTaskState is TaskViewModel.UpdateTaskState.Success) {
            viewModel.clearUpdateTaskState()
            delay(300) // Short delay to ensure server-side update is complete
            shouldRefresh = true // Trigger refresh
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Review") },

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
//                    Text(
//                       // text = task.description,
//                        style = MaterialTheme.typography.headlineMedium,
//                        fontWeight = FontWeight.Bold
//                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Task type chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskTypeChip(taskType = task.taskType)
                        Spacer(modifier = Modifier.width(8.dp))
                        // Add a second chip for crop type if available
                        val cropName = getCropNameFromJson(task.detailsJson)
                        if (!cropName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = cropName,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 1: Task Details
                    Text(
                        text = "Task Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Task details in key-value format
                    if (task.taskType.uppercase() == "SCOUTING") {
                        FormattedScoutingFields(task.detailsJson)
                    } else {
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
                                DetailRow(key.replaceFirstChar { it.uppercase() }, value)
                            }
                        } else {
                            Text("No details available")
                        }
                    }

                    // Location info
                    val locationInfo = getLocationFromJson(task.detailsJson)
                    if (!locationInfo.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = locationInfo,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Date
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDateTime(task.createdAt ?: ""),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Assigned user
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.assignedTo ?: task.createdBy,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 2: Description section if there's a detailed description

                    task.description.takeIf { it.length >0 }?.let {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Uploaded Images section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Uploaded Images",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Placeholder images row - this would be replaced with actual image loading logic
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SECTION 3: Task advice section if advice exists
                    if (advices.isNotEmpty()) {
                        Text(
                            text = "Task Advice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        advices.forEach { advice ->
                            SimpleAdviceItem(advice = advice)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // SECTION 4: Display implementation if exists (for any user)
                    val implementation = getImplementationFromJson(task.implementationJson)
                    if (!implementation.isNullOrEmpty()) {
                        Text(
                            text = "Implementation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Role-based actions
                    when (userRole) {
                        "MANAGER" -> {
                            // Manager - show Add Advice (Optional) field and Approve/Reject buttons
                            if (task.status.equals("submitted", ignoreCase = true)) {
                                // Only show advice input if no advices exist
                                if (advices.isEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Add Advice (Optional)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedTextField(
                                                value = comment,
                                                onValueChange = { comment = it },
                                                placeholder = { Text("Enter your advice or feedback for the supervisor") },
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 3
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Approve/Reject buttons
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.updateTaskStatus(taskId, "rejected", comment.takeIf { it.isNotBlank() })
                                                        comment = ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFFFD6D6),
                                                        contentColor = Color(0xFFFF3B30)
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

                                                Button(
                                                    onClick = {
                                                        viewModel.updateTaskStatus(taskId, "approved", comment.takeIf { it.isNotBlank() })
                                                        comment = ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFD6F2D6),
                                                        contentColor = Color(0xFF34C759)
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
                                            }
                                        }
                                    }
                                } else {
                                    // If advice exists, just show approve/reject buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.updateTaskStatus(taskId, "rejected", null)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFD6D6),
                                                contentColor = Color(0xFFFF3B30)
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

                                        Button(
                                            onClick = {
                                                viewModel.updateTaskStatus(taskId, "approved", null)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFD6F2D6),
                                                contentColor = Color(0xFF34C759)
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
                                    }
                                }
                            }
                        }
                        "SUPERVISOR" -> {
                            // Supervisor can implement approved tasks
                            // Only show implementation input if no implementation exists
                            if (task.status.equals("approved", ignoreCase = true) && getImplementationFromJson(task.implementationJson).isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Column {
                                    Text(
                                        text = "Implementation Details",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = implementationInput,
                                        onValueChange = { implementationInput = it },
                                        placeholder = { Text("Enter implementation details") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (implementationInput.isNotBlank()) {
                                                viewModel.addTaskImplementation(taskId, implementationInput)
                                                // Reset input but don't refresh yet - wait for success callback
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

// New detailed row component to match UI in image 2
@Composable
fun DetailRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label:",
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

// Scouting fields formatted nicely like in image 2
@Composable
fun FormattedScoutingFields(detailsJson: String?) {
    val json = try {
        detailsJson?.let { JSONObject(it) }
    } catch (e: Exception) {
        Log.e("ScoutingFields", "Error parsing JSON: ${e.message}")
        null
    }

    json?.let {
        // From image 2:
        DetailRow("Disease/Pest", it.optString("nameOfDisease", "None detected"))
        DetailRow("Fruits Count", it.optString("noOfFruitSeen", ""))
        DetailRow("Flowers Count", it.optString("noOfFlowersSeen", ""))

        // Additional fields from original code
        DetailRow("Row", it.optString("row", ""))
        DetailRow("Tree Number", it.optString("treeNo", ""))
        DetailRow("Fruits Dropped", it.optString("noOfFruitsDropped", ""))
    } ?: Text(
        text = "Error loading scouting details",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

// Simplified advice item
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

// Helper function to format date time
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

        dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm a"))
    } catch (e: Exception) {
        dateTime // Return original if parsing fails
    }
}

// Helper function to get location from JSON - outside of Composable function
private fun getLocationFromJson(detailsJson: String?): String? {
    return try {
        detailsJson?.let {
            val json = JSONObject(it)
            json.optString("location").takeIf { it.isNotBlank() }
        }
    } catch (e: Exception) {
        null
    }
}

// Helper function to get crop name from JSON - outside of Composable function
private fun getCropNameFromJson(detailsJson: String?): String? {
    return try {
        detailsJson?.let {
            val json = JSONObject(it)
            json.optString("cropName").takeIf { it.isNotBlank() }
        }
    } catch (e: Exception) {
        null
    }
}

// Helper function to parse implementation JSON - outside of Composable function
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