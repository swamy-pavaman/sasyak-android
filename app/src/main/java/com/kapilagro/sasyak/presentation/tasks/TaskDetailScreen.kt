package com.kapilagro.sasyak.presentation.tasks

import android.os.Build
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import android.util.Log
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.data.api.ImageUploadService
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.presentation.common.components.TaskTypeChip
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.AgroMuted
import com.kapilagro.sasyak.presentation.common.theme.AgroMutedForeground
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import com.kapilagro.sasyak.presentation.common.theme.ApprovedContainer
import com.kapilagro.sasyak.presentation.common.theme.ApprovedText
import com.kapilagro.sasyak.presentation.common.theme.RejectedContainer
import com.kapilagro.sasyak.presentation.common.theme.RejectedText
import com.kapilagro.sasyak.presentation.common.theme.SubmittedContainer
import com.kapilagro.sasyak.presentation.common.theme.SubmittedText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.isNotEmpty

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
    var isActivityExpanded by remember { mutableStateOf(false) }

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
        viewModel.getCurrentUserRole()
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

                            if (imageUrls.isEmpty()) {
                                imageUrls =
                                    listOf("http://13.203.61.201:9000/sasyak/SOWING/gallery_1748631470361.jpg")
                            }

                            ImageSlideshow(
                                imageUrls = imageUrls
                            )

                            // Title and description
                            Column(modifier = Modifier.padding(16.dp)) {
                                task.description.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                        overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                                        modifier = Modifier.animateContentSize()
                                    )

                                    val isTextLongEnough = it.length > 100 || it.lines().size > 2
                                    if (isTextLongEnough) {
                                        Spacer(modifier = Modifier.height(8.dp))
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

                    // Chips
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
                                val keysList = details.keys().asSequence().toList()
                                val displayKeys =
                                    if (isExpandedForDetails) keysList else keysList.take(4)

                                displayKeys.forEach { key ->
                                    val value = details.optString(key, "")
                                    DetailRow(
                                        key.replaceFirstChar { it.uppercase() },
                                        value
                                    )
                                }

                                if (keysList.size > 4) {
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
                            } else {
                                Text("No details available")
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

                    val implementations = getImplementationsFromJson(task.implementationJson)
                    if (advices.isNotEmpty() || implementations.isNotEmpty()) {
                        // Pair advices and implementations by index
                        val pairedItems = mutableListOf<Pair<TaskAdvice?, Implementation?>>()
                        val maxSize = maxOf(advices.size, implementations.size)
                        for (i in 0 until maxSize) {
                            val advice = if (i < advices.size) advices[i] else null
                            val implementation = if (i < implementations.size) implementations[i] else null
                            pairedItems.add(Pair(advice, implementation))
                        }



                        Text(
                            text = "Activity History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Limit to 2 items initially, show all if expanded
                            val displayItems = if (isActivityExpanded) pairedItems else pairedItems.take(2)

                            displayItems.forEach { pair ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        pair.first?.let { advice ->
                                            AdviceChatItem(
                                                advice = advice,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        if (pair.first != null && pair.second != null) {
                                            Divider(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                        pair.second?.let { implementation ->
                                            ImplementationChatItem(
                                                implementation = implementation,
                                                showPreviewDialog = { showPreviewDialog = it },
                                                imagesToPreview = { imagesToPreview = it },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 40.dp) // Indent implementation
                                            )
                                        }
                                    }
                                }
                            }

                            // Show More/Show Less toggle
                            if (pairedItems.size > 2) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isActivityExpanded = !isActivityExpanded }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isActivityExpanded) "Show Less" else "Show More",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Icon(
                                        imageVector = if (isActivityExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = if (isActivityExpanded) "Collapse history" else "Expand history",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
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
                                ) && advices.isNotEmpty()) ||
                                task.status.equals("implemented", ignoreCase = true)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = if (task.status.equals(
                                                "implemented",
                                                ignoreCase = true
                                            )
                                        )
                                            "Update Implementation" else "Implementation Details",
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
                                                    "Update your implementation based on manager's feedback"
                                                else
                                                    "Enter implementation details"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        enabled = updateTaskState !is TaskViewModel.UpdateTaskState.Loading
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

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

                                    if (uploadState is UploadState.Loading) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = AgroPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

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
                                                    "Update Implementation" else "Submit Implementation"
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
    // Handle empty imageUrls case
    if (imageUrls.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No images available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
        return
    }

    var currentImageIndex by remember { mutableStateOf(0) }
    var showPreview by remember { mutableStateOf(false) }
    val numberOfImages = imageUrls.size

    LaunchedEffect(currentImageIndex) {
        delay(3000)
        currentImageIndex = (currentImageIndex + 1) % numberOfImages
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(numberOfImages) {
                    detectDragGestures(
                        onDragEnd = {}
                    ) { _, dragAmount ->
                        val swipeThreshold = 50f
                        if (dragAmount.x > swipeThreshold) {
                            currentImageIndex = if (currentImageIndex > 0) {
                                currentImageIndex - 1
                            } else {
                                numberOfImages - 1
                            }
                        } else if (dragAmount.x < -swipeThreshold) {
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

@Composable
fun AdviceChatItem(advice: TaskAdvice, modifier: Modifier = Modifier) {
    var isAdviceExpanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.padding(12.dp)) {
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = advice.adviceText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (isAdviceExpanded) Int.MAX_VALUE else 3,
            overflow = if (isAdviceExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
            modifier = Modifier.animateContentSize()
        )
        val isTextLongEnough = advice.adviceText.length > 150 || advice.adviceText.lines().size > 3
        if (isTextLongEnough) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isAdviceExpanded) "Show Less" else "Show More",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { isAdviceExpanded = !isAdviceExpanded }
            )
        }
    }
}
@Composable
fun ImplementationChatItem(
    implementation: com.kapilagro.sasyak.presentation.tasks.Implementation,
    showPreviewDialog: (Boolean) -> Unit,
    imagesToPreview: (List<String>?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isImplementationExpanded by remember { mutableStateOf(false) }
    Log.d("ImplementationChatItem", "Rendering implementation: text=${implementation.text}, imageUrls=${implementation.imageUrls}, size=${implementation.imageUrls?.size ?: 0}")

    Column(modifier = modifier.padding(12.dp)) {
        Text(
            text = implementation.supervisorName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Handle image display with horizontal scrolling
        val imageUrls = implementation.imageUrls?.take(5) ?: emptyList()
        if (imageUrls.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .heightIn(max = 100.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                imageUrls.forEach { url ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .build(),
                        contentDescription = "Implementation image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                Log.d("ImplementationChatItem", "Opening preview dialog with ${implementation.imageUrls?.size} images")
                                imagesToPreview(implementation.imageUrls)
                                showPreviewDialog(true)
                            },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = painterResource(id = android.R.drawable.stat_sys_warning)
                    )
                }
                if ((implementation.imageUrls?.size ?: 0) > 5) {
                    Text(
                        text = "View ${implementation.imageUrls!!.size - 5} more image(s)",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 8.dp)
                            .clickable {
                                Log.d("ImplementationChatItem", "Opening preview dialog with ${implementation.imageUrls?.size} images")
                                imagesToPreview(implementation.imageUrls)
                                showPreviewDialog(true)
                            }
                    )
                }
            }
        } else {
            Text(
                text = "No implementation images available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Handle text display with show more/less
        implementation.text.takeIf { it.isNotBlank() }?.let { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0E141B),
                maxLines = if (isImplementationExpanded) Int.MAX_VALUE else 3,
                overflow = if (isImplementationExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                modifier = Modifier.animateContentSize()
            )

            // Check if the text is long enough to show the toggle
            val isTextLongEnough = text.length > 100 || text.lines().size > 2
            if (isTextLongEnough) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isImplementationExpanded) "Show Less" else "Show More",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { isImplementationExpanded = !isImplementationExpanded }
                        .padding(vertical = 4.dp)
                )
            }
        }

        Text(
            text = formatTimestamp(implementation.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4E7397)
        )
    }
}
@Composable
fun ImageLoader(
    url: String,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val density = LocalDensity.current // Access density for dp-to-px conversion

    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.byteStream()?.use { inputStream ->
                        val loadedBitmap = BitmapFactory.decodeStream(inputStream)
                        if (loadedBitmap != null) {
                            withContext(Dispatchers.Main) {
                                bitmap = loadedBitmap
                                Log.d("ImageLoader", "Successfully loaded image: $url")
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                error = "Failed to decode bitmap: $url"
                                Log.e("ImageLoader", "Failed to decode bitmap: $url")
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        error = "HTTP ${response.code}: $url"
                        Log.e("ImageLoader", "Failed to load image: HTTP ${response.code} - $url")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    error = "Network error: ${e.message}"
                    Log.e("ImageLoader", "Failed to load image $url: ${e.message}")
                }
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                clipToOutline = true
                // Use LocalDensity to convert 8.dp to pixels
                val cornerRadiusPx = with(density) { 8.dp.toPx() }
                background = android.graphics.drawable.ShapeDrawable(
                    android.graphics.drawable.shapes.RoundRectShape(
                        FloatArray(8) { cornerRadiusPx },
                        null,
                        null
                    )
                )
            }
        },
        modifier = modifier,
        update = { imageView ->
            when {
                bitmap != null -> {
                    imageView.setImageBitmap(bitmap)
                    Log.d("ImageLoader", "Displaying bitmap for: $url")
                }
                error != null -> {
                    imageView.setImageResource(android.R.drawable.stat_sys_warning)
                    Log.d("ImageLoader", "Displaying error drawable for: $url")
                }
                else -> {
                    imageView.setImageResource(android.R.drawable.ic_menu_gallery) // Placeholder while loading
                    Log.d("ImageLoader", "Showing placeholder for: $url")
                }
            }
        }
    )
}
private fun formatDateTime(dateTime: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateTime) ?: Date()
        SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
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
    // Log the input JSON for debugging
    Log.d("getImplementationsFromJson", "Input JSON: $implementationJson")

    if (implementationJson.isNullOrEmpty() || implementationJson == "{}" || implementationJson == "[]") {
        Log.d("getImplementationsFromJson", "JSON is null, empty, or invalid, returning empty list")
        return emptyList()
    }

    return try {
        val jsonArray = JSONArray(implementationJson)
        val implementations = mutableListOf<Implementation>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val imageUrls = obj.optJSONArray("imageUrls")?.let { array ->
                (0 until array.length()).mapNotNull { index ->
                    try {
                        array.getString(index).takeIf { it.isNotEmpty() }
                    } catch (e: Exception) {
                        Log.e("getImplementationsFromJson", "Error parsing image URL at index $index: ${e.message}")
                        null
                    }
                }
            } ?: emptyList()
            Log.d("getImplementationsFromJson", "Parsed imageUrls for object $i: $imageUrls")
            implementations.add(
                Implementation(
                    text = obj.optString("text", ""),
                    timestamp = obj.optLong("timestamp", 0L),
                    supervisorName = obj.optString("supervisorName", "Supervisor"),
                    imageUrls = imageUrls
                )
            )
        }
        Log.d("getImplementationsFromJson", "Parsed ${implementations.size} implementations")
        implementations.sortedByDescending { it.timestamp }
    } catch (e: Exception) {
        Log.e("getImplementationsFromJson", "Error parsing JSON array: ${e.message}")
        try {
            val jsonObject = JSONObject(implementationJson)
            val imageUrls = jsonObject.optJSONArray("imageUrls")?.let { array ->
                (0 until array.length()).mapNotNull { index ->
                    try {
                        array.getString(index).takeIf { it.isNotEmpty() }
                    } catch (e: Exception) {
                        Log.e("getImplementationsFromJson", "Error parsing single image URL at index $index: ${e.message}")
                        null
                    }
                }
            } ?: emptyList()
            Log.d("getImplementationsFromJson", "Parsed single object imageUrls: $imageUrls")
            listOf(
                Implementation(
                    text = jsonObject.optString("text", ""),
                    timestamp = jsonObject.optLong("timestamp", 0L),
                    supervisorName = jsonObject.optString("supervisorName", "Supervisor"),
                    imageUrls = imageUrls
                )
            )
        } catch (e2: Exception) {
            Log.e("getImplementationsFromJson", "Error parsing single JSON object: ${e2.message}")
            emptyList()
        }
    }
}
data class Implementation(
    val text: String,
    val timestamp: Long,
    val supervisorName: String,
    val imageUrls: List<String>?
)

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val date = Date(timestamp)
        SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        "Unknown time"
    }
}

private sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Error(val message: String) : UploadState()
}

sealed interface ChatItem {
    val timestamp: Long
    data class Advice(val advice: TaskAdvice) : ChatItem {
        override val timestamp: Long
            get() = advice.createdAt?.let {
                try {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it)?.time ?: 0L
                } catch (e: Exception) {
                    Log.e("ChatItem", "Failed to parse date: ${e.message}")
                    0L
                }
            } ?: 0L
    }
    data class Implementation(val implementation: com.kapilagro.sasyak.presentation.tasks.Implementation) : ChatItem {
        override val timestamp: Long
            get() = implementation.timestamp
    }
}