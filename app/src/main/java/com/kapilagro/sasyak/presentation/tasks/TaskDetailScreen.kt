package com.kapilagro.sasyak.presentation.tasks

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.presentation.common.components.TaskTypeChip
import com.kapilagro.sasyak.presentation.common.theme.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive


import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val addAdviceState by viewModel.addAdviceState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var comment by remember { mutableStateOf("") }
    var implementationInput by remember { mutableStateOf("") }
    var shouldRefresh by remember { mutableStateOf(false) }

    LaunchedEffect(taskId, shouldRefresh) {
        viewModel.loadTaskDetail(taskId)
        viewModel.getCurrentUserRole()
        if (shouldRefresh) {
            shouldRefresh = false
        }
    }

    LaunchedEffect(updateTaskState) {
        if (updateTaskState is TaskViewModel.UpdateTaskState.Success) {
            viewModel.clearUpdateTaskState()
            delay(300)
            shouldRefresh = true
        }
    }

    LaunchedEffect(addAdviceState) {
        if (addAdviceState is TaskViewModel.AddAdviceState.Success) {
            viewModel.clearAddAdviceState()
            comment = ""
            delay(300)
            shouldRefresh = true
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
                ) {
                    // Image slideshow with title and description
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            var imageUrls = Json.parseToJsonElement(task.imagesJson.toString())
                                .jsonArray
                                .map { it.jsonPrimitive.content }

                            if (imageUrls.isEmpty()) {
                                imageUrls = listOf("http://13.203.61.201:9000/sasyak/SOWING/gallery_1748631470361.jpg")
                            }

                            ImageSlideshow(
                                imageUrls = imageUrls
                            )

                            // Image slideshow with dots
//                            ImageSlideshow(
//
////                                imageUrls = Json.parseToJsonElement(task.imagesJson.toString())
////                                .jsonArray
////                                .map { it.jsonPrimitive.content }
//                                imageUrls
//                            )



                            // Title and description
                            Column(modifier = Modifier.padding(16.dp)) {
//                                Text(
//                                    text = task.taskType,
//                                    style = MaterialTheme.typography.headlineSmall,
//                                    fontWeight = FontWeight.Bold
//                                )
//
//                                Spacer(modifier = Modifier.height(8.dp))

                                task.description.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chips at the bottom of images
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskTypeChip(taskType = task.taskType)
                        Spacer(modifier = Modifier.width(8.dp))
                        TaskStatusChip(taskStatus = task.status)
                        Spacer(modifier = Modifier.width(8.dp))
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

                    // Task details
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        if (task.taskType.uppercase() == "SCOUTING") {
                            FormattedScoutingFields(task.detailsJson)
                        } else {
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (advices.isNotEmpty()) {
                        Text(
                            text = "Task Advice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        advices.forEach { advice ->
                            SimpleAdviceItem(advice = advice)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    val implementation = getImplementationFromJson(task.implementationJson)
                    if (!implementation.isNullOrEmpty()) {
                        Text(
                            text = "Implementation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
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
                            if (task.status.equals("submitted", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(16.dp))

                                // Advice input section
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Add Advice",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = comment,
                                            onValueChange = { comment = it },
                                            placeholder = { Text("Enter your advice or feedback for the supervisor") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 3,
                                            enabled = addAdviceState !is TaskViewModel.AddAdviceState.Loading
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = {
                                                if (comment.isNotBlank()) {
                                                    viewModel.addTaskAdvice(taskId, comment)
                                                }
                                            },
                                            enabled = comment.isNotBlank() && addAdviceState !is TaskViewModel.AddAdviceState.Loading,
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            if (addAdviceState is TaskViewModel.AddAdviceState.Loading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Outlined.Add,
                                                    contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Submit Advice")
                                            }
                                        }

                                        if (addAdviceState is TaskViewModel.AddAdviceState.Error) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = (addAdviceState as TaskViewModel.AddAdviceState.Error).message,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Approve/Reject buttons
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.updateTaskStatus(taskId, "rejected")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFFD6D6),
                                            contentColor = Color(0xFFFF3B30)
                                        ),
                                        modifier = Modifier.weight(1f),
                                        enabled = updateTaskState !is TaskViewModel.UpdateTaskState.Loading
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
                                            viewModel.updateTaskStatus(taskId, "approved")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFD6F2D6),
                                            contentColor = Color(0xFF34C759)
                                        ),
                                        modifier = Modifier.weight(1f),
                                        enabled = updateTaskState !is TaskViewModel.UpdateTaskState.Loading
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Approve")
                                    }
                                }

                                if (updateTaskState is TaskViewModel.UpdateTaskState.Error) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = (updateTaskState as TaskViewModel.UpdateTaskState.Error).message,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                        "SUPERVISOR" -> {
                            if (task.status.equals("approved", ignoreCase = true) && getImplementationFromJson(task.implementationJson).isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                                        minLines = 3,
                                        enabled = updateTaskState !is TaskViewModel.UpdateTaskState.Loading
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (implementationInput.isNotBlank()) {
                                                viewModel.addTaskImplementation(taskId, implementationInput)
                                                implementationInput = ""
                                            }
                                        },
                                        enabled = implementationInput.isNotBlank() && updateTaskState !is TaskViewModel.UpdateTaskState.Loading,
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        if (updateTaskState is TaskViewModel.UpdateTaskState.Loading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.PlayArrow,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Submit Implementation")
                                        }
                                    }

                                    if (updateTaskState is TaskViewModel.UpdateTaskState.Error) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = (updateTaskState as TaskViewModel.UpdateTaskState.Error).message,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
fun ImageSlideshow(imageUrls: List<String>) {
    var currentImageIndex by remember { mutableStateOf(0) }
    var showPreview by remember { mutableStateOf(false) }
    val numberOfImages = imageUrls.size

    // Auto-scroll effect
    LaunchedEffect(currentImageIndex) {
        delay(3000)
        currentImageIndex = (currentImageIndex + 1) % numberOfImages
    }

    // Smooth transition animation
    val animatedOffset by animateFloatAsState(
        targetValue = -currentImageIndex.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        // Image container with swipe support
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(numberOfImages) {
                    detectDragGestures(
                        onDragEnd = {
                            // Handle swipe completion if needed
                        }
                    ) { _, dragAmount ->
                        val swipeThreshold = 50f
                        if (dragAmount.x > swipeThreshold) {
                            // Swipe right - previous image
                            currentImageIndex = if (currentImageIndex > 0) {
                                currentImageIndex - 1
                            } else {
                                numberOfImages - 1
                            }
                        } else if (dragAmount.x < -swipeThreshold) {
                            // Swipe left - next image
                            currentImageIndex = (currentImageIndex + 1) % numberOfImages
                        }
                    }
                }
                .clickable {
                    showPreview = true
                }
        ) {
            AsyncImage(
                model = imageUrls[currentImageIndex],
                contentDescription = "Image ${currentImageIndex + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Indicator dots with smooth transitions
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(numberOfImages) { index ->
                val isSelected = index == currentImageIndex
                val animatedSize by animateFloatAsState(
                    targetValue = if (isSelected) 10f else 8f,
                    animationSpec = tween(200),
                    label = "dotSize"
                )
                val animatedAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.5f,
                    animationSpec = tween(200),
                    label = "dotAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(animatedSize.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = animatedAlpha))
                        .clickable {
                            currentImageIndex = index
                        }
                )
            }
        }
    }

    // Full-screen preview dialog
    if (showPreview) {
        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showPreview = false }
            ) {
                var previewIndex by remember { mutableStateOf(currentImageIndex) }

                // Full screen image with swipe support
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(numberOfImages) {
                            detectDragGestures { _, dragAmount ->
                                val swipeThreshold = 100f
                                if (dragAmount.x > swipeThreshold) {
                                    previewIndex = if (previewIndex > 0) {
                                        previewIndex - 1
                                    } else {
                                        numberOfImages - 1
                                    }
                                } else if (dragAmount.x < -swipeThreshold) {
                                    previewIndex = (previewIndex + 1) % numberOfImages
                                }
                            }
                        }
                ) {
                    AsyncImage(
                        model = imageUrls[previewIndex],
                        contentDescription = "Preview image ${previewIndex + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Close button
                IconButton(
                    onClick = { showPreview = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close preview",
                        tint = Color.White
                    )
                }

                // Preview indicator dots
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(numberOfImages) { index ->
                        val isSelected = index == previewIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White.copy(alpha = if (isSelected) 1f else 0.5f)
                                )
                                .clickable {
                                    previewIndex = index
                                }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun TaskStatusChip(taskStatus: String) {
    val (backgroundColor, textColor) = when (taskStatus.lowercase()) {
        "submitted" -> Pair(WarningAccent.copy(alpha = 0.1f), WarningAccent)
        "approved" -> Pair(PrimaryAccent.copy(alpha = 0.1f), PrimaryAccent)
        "rejected" -> Pair(ErrorAccent.copy(alpha = 0.1f), ErrorAccent)
        else -> Pair(AgroMuted.copy(alpha = 0.1f), AgroMutedForeground)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier.height(30.dp)
    ) {
        Text(
            text = taskStatus.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

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

@Composable
fun FormattedScoutingFields(detailsJson: String?) {
    val json = try {
        detailsJson?.let { JSONObject(it) }
    } catch (e: Exception) {
        Log.e("ScoutingFields", "Error parsing JSON: ${e.message}")
        null
    }

    json?.let {
        DetailRow("Disease/Pest", it.optString("nameOfDisease", "None detected"))
        DetailRow("Fruits Count", it.optString("noOfFruitSeen", ""))
        DetailRow("Flowers Count", it.optString("noOfFlowersSeen", ""))
        DetailRow("Row", it.optString("row", ""))
        DetailRow("Tree Number", it.optString("treeNo", ""))
        DetailRow("Fruits Dropped", it.optString("noOfFruitsDropped", ""))
    } ?: Text(
        text = "Error loading scouting details",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SimpleAdviceItem(advice: TaskAdvice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    text = formatDateTime(advice.createdAt ?: ""),
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

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateTime(dateTime: String): String {
    return try {
        val dt = if (dateTime.contains(".")) {
            val trimmed = dateTime.split(".")[0]
            LocalDateTime.parse(trimmed)
        } else {
            LocalDateTime.parse(dateTime)
        }
        dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm a"))
    } catch (e: Exception) {
        dateTime
    }
}

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
