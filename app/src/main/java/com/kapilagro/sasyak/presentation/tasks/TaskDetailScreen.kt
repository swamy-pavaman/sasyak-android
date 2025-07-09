package com.kapilagro.sasyak.presentation.tasks


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.data.api.ImageUploadService
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.presentation.common.components.TaskTypeChip
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.isNotEmpty

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Int,
    onBackClick: () -> Unit,
    navController: NavController,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    imageUploadService: ImageUploadService,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val taskDetailState by viewModel.taskDetailState.collectAsState()
    val updateTaskState by viewModel.updateTaskState.collectAsState()
    val addAdviceState by viewModel.addAdviceState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var comment by remember { mutableStateOf("") }
    var implementationInput by rememberSaveable { mutableStateOf("") }
    var shouldRefresh by remember { mutableStateOf(false) }
    var implementationImages by remember { mutableStateOf<List<File>?>(null) }
    var uploadState by remember { mutableStateOf<UploadState>(UploadState.Idle) }
    val scope = rememberCoroutineScope()
    var imagesToPreview by remember { mutableStateOf<List<String>?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showAllAdvices by remember { mutableStateOf(false) }
    var showAllImplementations by remember { mutableStateOf(false) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<List<File>>(
            "selectedImages",
            emptyList()
        )
            ?.collect { files ->
                implementationImages = files
            }
    }

    LaunchedEffect(taskId, shouldRefresh) {
        viewModel.loadTaskDetail(taskId)
        //viewModel.getCurrentUserRole()
        if (shouldRefresh) {
            shouldRefresh = false
        }
    }

    LaunchedEffect(updateTaskState) {
        if (updateTaskState is TaskViewModel.UpdateTaskState.Success) {
            viewModel.clearUpdateTaskState()
            implementationImages = null
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
                var isExpanded by remember { mutableStateOf(false) }
                var isExpandedForDetails by remember { mutableStateOf(false) }

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
                            Log.d("imageUrls", imageUrls.toString())
                            if (imageUrls.isNullOrEmpty()) {
                                imageUrls =
                                    listOf("https://minio.kapilagro.com:9000/sasyak/placeholder.png")
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
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = if (isExpanded) Int.MAX_VALUE else 3, // Limit to 2 lines when not expanded
                                        overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis, // Ellipsis for truncation
                                        modifier = Modifier.animateContentSize() // Smooth animation for expansion/collapse
                                    )

                                    // Check if text is long enough to need a Show More button
                                    val isTextLongEnough = it.length > 100 || it.lines().size > 2
                                    if (isTextLongEnough) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Show More/Less button
                                        Text(
                                            text = if (isExpanded) "Show Less" else "Show More",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .clickable { isExpanded = !isExpanded }
                                                .padding(vertical = 4.dp)
                                        )
                                    }
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
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 4.dp
                                    ),
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
                                // Collect keys with non-null and non-empty values into a list
                                val keysList = details.keys().asSequence()
                                    .filter { key -> details.optString(key, "").isNotEmpty() }
                                    .toList()

                                // Show "No details available" if no valid key-value pairs exist
                                if (keysList.isEmpty()) {
                                    Text(
                                        text = "No details available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                } else {
                                    // Limit to first 6 keys when not expanded
                                    val displayKeys = if (isExpandedForDetails) keysList else keysList.take(6)

                                    // Display key-value pairs
                                    displayKeys.forEach { key ->
                                        val value = details.optString(key, "")
                                        DetailRow(
                                            key.replaceFirstChar { it.uppercase() },
                                            value
                                        )
                                    }

                                    // Show arrow icon if there are more than 6 keys
                                    if (keysList.size > 6) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    isExpandedForDetails = !isExpandedForDetails
                                                }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isExpandedForDetails) "Show Less" else "Show More",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                            Icon(
                                                imageVector = if (isExpandedForDetails) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                contentDescription = if (isExpandedForDetails) "Collapse details" else "Expand details",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "No details available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
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
                                Spacer(modifier = Modifier.height(8.dp))

                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))
                    if (advices.isNotEmpty()) {
                        Text(
                            text = "Task Advice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                        val reversedAdvices = advices
                        if (showAllAdvices) {
                            // Show all advices
                            reversedAdvices.forEach { advice ->
                                SimpleAdviceItem(advice = advice)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (reversedAdvices.size > 1) {
                                // Show "View Less" button
                                TextButton(
                                    onClick = { showAllAdvices = false },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("View Less")
                                }
                            }
                        } else {
                            // Show only the first advice
                            SimpleAdviceItem(advice = reversedAdvices.first())
                            Spacer(modifier = Modifier.height(8.dp))
                            if (reversedAdvices.size > 1) {
                                // Show "View More" button if there are more advices
                                TextButton(
                                    onClick = { showAllAdvices = true },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("View More (${reversedAdvices.size - 1} more)")
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No advices available",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp,horizontal = 16.dp)
                        )
                    }

                    val implementations = getImplementationsFromJson(task.implementationJson)
                    Log.d("implementations", implementations.toString())
                    if (implementations.isNotEmpty()) {
                        Text(
                            text = "Implementation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (showAllImplementations) {
                            // Show all implementations
                            implementations.forEach { implementation ->
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
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = implementation.supervisorName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = formatTimestamp(implementation.timestamp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = implementation.text,
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        // Display implementation images if available
                                        if (implementation.imageUrls?.isNotEmpty() == true) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                implementation.imageUrls.forEach { url ->
                                                    Box(modifier = Modifier.size(80.dp)) {
                                                        Card(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clickable {
                                                                    imagesToPreview = listOf(url) // Preview single image
                                                                    showPreviewDialog = true
                                                                },
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            AsyncImage(
                                                                model = url,
                                                                contentDescription = "Implementation image",
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (implementations.size > 1) {
                                // Show "View Less" button
                                TextButton(
                                    onClick = { showAllImplementations = false },
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text("View Less")
                                }
                            }
                        } else {
                            // Show only the first implementation
                            val firstImplementation = implementations.first()
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = firstImplementation.supervisorName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = formatTimestamp(firstImplementation.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = firstImplementation.text,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    // Display implementation images if available
                                    if (firstImplementation.imageUrls?.isNotEmpty() == true) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            firstImplementation.imageUrls.forEach { url ->
                                                Box(modifier = Modifier.size(80.dp)) {
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clickable {
                                                                imagesToPreview = listOf(url) // Preview single image
                                                                showPreviewDialog = true
                                                            },
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        AsyncImage(
                                                            model = url,
                                                            contentDescription = "Implementation image",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (implementations.size > 1) {
                                // Show "View More" button if there are more implementations
                                TextButton(
                                    onClick = { showAllImplementations = true },
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text("View More (${implementations.size - 1} more)")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (showPreviewDialog && imagesToPreview != null) {
                        Dialog(
                            onDismissRequest = {
                                showPreviewDialog = false
                                imagesToPreview = null
                            },
                            properties = DialogProperties(usePlatformDefaultWidth = false)
                        ) {
                            ImageSlideshow(imageUrls = imagesToPreview!!)
                        }
                    }

                    // Role-based actions
                    when (userRole) {
                        "MANAGER" -> {
                            if (task.status.equals("submitted", ignoreCase = true) ||
                                task.status.equals("implemented", ignoreCase = true)
                            ) {

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
                                            text = if (task.status.equals(
                                                    "implemented",
                                                    ignoreCase = true
                                                )
                                            )
                                                "Add Follow-up Advice" else "Add Advice",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = comment,
                                            onValueChange = { comment = it },
                                            placeholder = {
                                                Text(
                                                    if (task.status.equals(
                                                            "implemented",
                                                            ignoreCase = true
                                                        )
                                                    )
                                                        "Enter follow-up advice or approve the implementation"
                                                    else
                                                        "Enter your advice or feedback for the supervisor"
                                                )
                                            },
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

                                // Approve/Reject buttons - only show if there's an implementation
                                if (task.status.equals(
                                        "submitted",
                                        ignoreCase = true
                                    ) || task.status.equals("implemented", ignoreCase = true)
                                ) {
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
                            if ((task.status.equals(
                                    "submitted",
                                    ignoreCase = true
                                ) && advices.isNotEmpty())
                                || task.status.equals("implemented", ignoreCase = true)
                                || task.assignedTo != null
                            ) {

                                Spacer(modifier = Modifier.height(16.dp))

                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = if (task.status.equals(
                                                "implemented",
                                                ignoreCase = true
                                            )
                                        )
                                            "Implementation Details" else "Implementation Details",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = implementationInput,
                                        onValueChange = { implementationInput = it },
                                        placeholder = {
                                            Text(
                                                if (task.status.equals(
                                                        "implemented",
                                                        ignoreCase = true
                                                    )
                                                )
                                                    "Add your implementation based on manager's feedback"
                                                else
                                                    "Enter implementation details"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        enabled = updateTaskState !is TaskViewModel.UpdateTaskState.Loading
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Image upload section
                                    Text(
                                        text = "Upload Photos",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    Column {
                                        Button(
                                            onClick = {
                                                navController.navigate(
                                                    Screen.ImageCapture.createRoute(
                                                        "TASK_$taskId"
                                                    )
                                                ) {
                                                    launchSingleTop = true
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            enabled = updateTaskState !is TaskViewModel.UpdateTaskState.Loading &&
                                                    uploadState !is UploadState.Loading
                                        ) {
                                            Text("Select Photos")
                                        }

                                        if (implementationImages != null && implementationImages!!.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                implementationImages!!.forEach { file ->
                                                    Box(modifier = Modifier.size(80.dp)) {
                                                        Card(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clickable { /* Optional: Add preview */ },
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Image(
                                                                painter = rememberAsyncImagePainter(
                                                                    file
                                                                ),
                                                                contentDescription = "Selected image",
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                implementationImages =
                                                                    implementationImages?.filter { it != file }
                                                            },
                                                            modifier = Modifier
                                                                .align(Alignment.TopEnd)
                                                                .size(24.dp)
                                                                .offset(x = 4.dp, y = (-4).dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "Remove image",
                                                                tint = Color.White,
                                                                modifier = Modifier.background(
                                                                    color = Color.Black.copy(alpha = 0.6f),
                                                                    shape = RoundedCornerShape(12.dp)
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Loading indicator for upload
                                    if (uploadState is UploadState.Loading) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = AgroPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // Error message for upload
                                    if (uploadState is UploadState.Error) {
                                        Text(
                                            text = (uploadState as UploadState.Error).message,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Button(
                                        onClick = {
                                            if (implementationInput.isNotBlank()) {
                                                scope.launch(ioDispatcher) {
                                                    // Upload images if any
                                                    val imageUrls =
                                                        if (implementationImages?.isNotEmpty() == true) {
                                                            uploadState = UploadState.Loading
                                                            val uploadResult =
                                                                imageUploadService.uploadImages(
                                                                    implementationImages!!,
                                                                    "TASK_$taskId"
                                                                )
                                                            when (uploadResult) {
                                                                is ApiResponse.Success -> uploadResult.data
                                                                is ApiResponse.Error -> {
                                                                    uploadState =
                                                                        UploadState.Error("Image upload failed: ${uploadResult.errorMessage}")
                                                                    return@launch
                                                                }

                                                                is ApiResponse.Loading -> {
                                                                    uploadState =
                                                                        UploadState.Loading
                                                                    return@launch
                                                                }
                                                            }
                                                        } else {
                                                            emptyList()
                                                        }

                                                    // Submit implementation with text and image URLs
                                                    viewModel.addTaskImplementation(
                                                        taskId,
                                                        implementationInput,
                                                        imageUrls,
                                                        task.implementationJson
                                                    )
                                                    implementationInput = ""
                                                    implementationImages = null
                                                    uploadState = UploadState.Idle
                                                }
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
                                            Text(
                                                if (task.status.equals(
                                                        "implemented",
                                                        ignoreCase = true
                                                    )
                                                )
                                                    "Add Implementation" else "Submit Implementation"
                                            )
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
        "submitted" -> Pair(SubmittedContainer, SubmittedText)
        "approved" -> Pair(ApprovedContainer, ApprovedText)
        "rejected" -> Pair(RejectedContainer, RejectedText)
        else -> Pair(AgroMuted, AgroMutedForeground)
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
        val fields = listOf(
            "Disease" to it.optString("nameOfDisease", "None detected"),
            "Fruits Count" to it.optString("noOfFruitSeen", ""),
            "Flowers Count" to it.optString("noOfFlowersSeen", ""),
            "Row" to it.optString("row", ""),
            "Tree Number" to it.optString("treeNo", ""),
            "Fruits Dropped" to it.optString("noOfFruitsDropped", ""),
            "Valve" to it.optString("valveName", ""),
            "Due Date" to it.optString("dueDate","noDue date")
        )

        // Filter out empty or blank values and display only valid ones
        fields.forEach { (label, value) ->
            if (value != "null") {
                DetailRow(label, value)
            }
        }
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = advice.managerName ?: "Manager",
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
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val parsedDateTime = LocalDateTime.parse(dateTime, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a")
        parsedDateTime.format(outputFormatter)

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

private fun getImplementationsFromJson(implementationJson: String?): List<Implementation> {
    if (implementationJson == null || implementationJson == "{}" || implementationJson.isEmpty()) {
        return emptyList()
    }
    return try {
        val jsonArray = JSONArray(implementationJson)
        val implementations = mutableListOf<Implementation>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val imageUrls = obj.optJSONArray("imageUrls")?.let { array ->
                List(array.length()) { array.getString(it) }
            }
            implementations.add(
                Implementation(
                    text = obj.optString("text"),
                    timestamp = obj.optLong("timestamp"),
                    supervisorName = obj.optString("supervisorName", "Supervisor"),
                    imageUrls = imageUrls
                )
            )
        }
        implementations.sortedByDescending { it.timestamp }
    } catch (e: Exception) {
        try {
            val jsonObject = JSONObject(implementationJson)
            val imageUrls = jsonObject.optJSONArray("imageUrls")?.let { array ->
                List(array.length()) { array.getString(it) }
            }
            listOf(
                Implementation(
                    text = jsonObject.optString("text"),
                    timestamp = jsonObject.optLong("timestamp"),
                    supervisorName = jsonObject.optString("supervisorName", "Supervisor"),
                    imageUrls = imageUrls
                )
            )
        } catch (e2: Exception) {
            emptyList()
        }
    }
}

data class Implementation(
    val text: String,
    val timestamp: Long,
    val supervisorName: String,
    val imageUrls: List<String>? = null
)

private fun formatTimestamp(timestamp: Long): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instant = java.time.Instant.ofEpochMilli(timestamp)
            val dateTime =
                java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
            dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm a"))
        } else {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm a", Locale.getDefault())
            format.format(date)
        }
    } catch (e: Exception) {
        "Unknown time"
    }
}
/*
@RequiresApi(Build.VERSION_CODES.O)
fun mergeAndSortCommunications(
    advices: List<TaskAdvice>,
    implementations: List<Implementation>
): List<CommunicationItem> {
    // Function to convert TaskAdvice.createdAt (string) to milliseconds
    fun parseAdviceTimestamp(createdAt: String): Long {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dateTime = LocalDateTime.parse(createdAt, formatter)
            dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            Log.e("CommunicationMerge", "Error parsing advice timestamp: $createdAt, ${e.message}")
            0L // Fallback to 0 to avoid crashes, will sort to earliest
        }
    }
    val adviceItems = advices.map { advice ->
        CommunicationItem.Advice(advice) to parseAdviceTimestamp(advice.createdAt ?: "")
    }
    val implementationItems = implementations.map { implementation ->
        CommunicationItem.Implementation(implementation) to implementation.timestamp
    }
    return (adviceItems + implementationItems)
        .sortedByDescending { it.second } // Sort by timestamp
        .map { it.first } // Extract the CommunicationItem
}

sealed class CommunicationItem {
    data class Advice(val advice: TaskAdvice) : CommunicationItem()
    data class Implementation(val implementation: com.kapilagro.sasyak.presentation.tasks.Implementation) : CommunicationItem()
}
*/
private sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Error(val message: String) : UploadState()
}