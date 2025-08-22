package com.kapilagro.sasyak.presentation.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkInfo
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    onBackClick: () -> Unit,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val uploadJobs by viewModel.uploadJobs.collectAsState()
    val taskJobs by viewModel.taskUploadJobs.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Upload Queue") },
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section 1 → Task uploads
            item {
                Text("Task Uploads", style = MaterialTheme.typography.titleLarge)
            }
            if (taskJobs.isEmpty()) {
                item {
                    Text("No active task uploads.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(taskJobs, key = { it.id }) { job ->
                    TaskJobCard(job = job)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Section 2 → File uploads
            item {
                Text("File Uploads", style = MaterialTheme.typography.titleLarge)
            }
            if (uploadJobs.isEmpty()) {
                item {
                    Text("No active file uploads.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(uploadJobs, key = { it.id }) { job ->
                    UploadJobCard(job = job)
                }
            }
        }
    }
}

@Composable
fun UploadJobCard(
    job: UploadJobUiState,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val progress = if (job.totalFiles > 0) {
        job.uploadedFiles.toFloat() / job.totalFiles.toFloat()
    } else 0f

    val statusText = when (job.status) {
        WorkInfo.State.ENQUEUED -> "Pending..."
        WorkInfo.State.RUNNING -> "Uploading ${job.uploadedFiles} of ${job.totalFiles} media"
        WorkInfo.State.SUCCEEDED -> "Upload complete"
        WorkInfo.State.FAILED -> "Upload failed"
        WorkInfo.State.BLOCKED -> "Waiting..."
        WorkInfo.State.CANCELLED -> "Cancelled"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Task ID: ${job.taskId}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Type: ${job.taskType}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Folder: ${job.folder}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Created: ${formatTimestamp(job.enqueuedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Show progress bar for running or successfully completed jobs
            if (job.status == WorkInfo.State.RUNNING || job.status == WorkInfo.State.SUCCEEDED) {
                LinearProgressIndicator(progress = progress
                    , modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (job.status == WorkInfo.State.RUNNING || job.status == WorkInfo.State.ENQUEUED) {
                    OutlinedButton(onClick = { viewModel.cancelJob(job.id) }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun TaskJobCard(
    job: TaskUploadJobUiState,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val statusText = when (job.status) {
        WorkInfo.State.ENQUEUED -> "Pending..."
        WorkInfo.State.RUNNING -> "Uploading task..."
        WorkInfo.State.SUCCEEDED -> "Task upload complete"
        WorkInfo.State.FAILED -> "Task upload failed"
        WorkInfo.State.BLOCKED -> "Waiting..."
        WorkInfo.State.CANCELLED -> "Cancelled"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Type: ${job.taskType}", style = MaterialTheme.typography.titleMedium)
            Text("Description: ${job.description}", style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Text("Created: ${formatTimestamp(job.enqueuedAt)}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))

            Text(statusText, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (job.status == WorkInfo.State.RUNNING || job.status == WorkInfo.State.ENQUEUED) {
                    OutlinedButton(onClick = { viewModel.cancelJob(job.id) }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}



private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "Unknown time"
    // Format: Jul 21, 2025 11:49
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}