package com.kapilagro.sasyak.presentation.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkInfo
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel = hiltViewModel()
) {
    val uploadJobs by viewModel.uploadJobs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Queue") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uploadJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No active uploads.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uploadJobs, key = { it.id }) { job ->
                    UploadJobCard(job = job)
                }
            }
        }
    }
}

@Composable
fun UploadJobCard(job: UploadJobUiState) {
    val progress = if (job.totalFiles > 0) {
        job.uploadedFiles.toFloat() / job.totalFiles.toFloat()
    } else {
        0f
    }

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
                text = "Type: ${job.folder}",
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
                LinearProgressIndicator(
                    progress =  progress,
                    modifier = Modifier.fillMaxWidth()
                )
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