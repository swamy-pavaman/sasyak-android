package com.kapilagro.sasyak.presentation.common.image

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCaptureScreen(
    folder: String,
    maxMedia: Int = 5,
    onMediaSelected: (List<String>) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ImageCaptureViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedMediaUris by viewModel.selectedMediaUris.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isRecording by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var videoCapture: VideoCapture<Recorder>? by remember { mutableStateOf(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        uris.take(maxMedia - selectedMediaUris.size).forEach { uri ->
            viewModel.addMediaUri(uri.toString())
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture Media") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedMediaUris.isNotEmpty()) {
                        TextButton(
                            onClick = { onMediaSelected(selectedMediaUris) },
                            enabled = selectedMediaUris.isNotEmpty()
                        ) {
                            Text("Done (${selectedMediaUris.size})")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { context ->
                            PreviewView(context).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            imageCapture = ImageCapture.Builder().build()
                            val recorder = Recorder.Builder()
                                .setExecutor(cameraExecutor)
                                .build()
                            videoCapture = VideoCapture.withOutput(recorder)
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture)
                            } catch (exc: Exception) {
                                // Handle exception
                            }
                        }, ContextCompat.getMainExecutor(context))
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (selectedMediaUris.size < maxMedia) {
                                    galleryLauncher.launch("image/*,video/*")
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = "Gallery")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        FloatingActionButton(
                            onClick = {
                                if (selectedMediaUris.size < maxMedia) {
                                    captureImage(context, imageCapture) { uri ->
                                        viewModel.addMediaUri(uri.toString())
                                    }
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = if (selectedMediaUris.size >= maxMedia) Color.Gray else MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Camera, contentDescription = "Capture Image")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        FloatingActionButton(
                            onClick = {
                                if (selectedMediaUris.size < maxMedia) {
                                    if (isRecording) {
                                        stopVideoRecording(activeRecording)
                                        isRecording = false
                                        activeRecording = null
                                    } else {
                                        captureVideo(context, videoCapture) { uri, recording ->
                                            viewModel.addMediaUri(uri.toString())
                                            activeRecording = recording
                                        }
                                        isRecording = true
                                    }
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = if (selectedMediaUris.size >= maxMedia || isRecording) Color.Gray else MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                                contentDescription = if (isRecording) "Stop Video" else "Record Video"
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera and audio permissions required")
                        Button(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }) {
                            Text("Grant Permissions")
                        }
                    }
                }
            }

            if (selectedMediaUris.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Selected Media (${selectedMediaUris.size}/$maxMedia)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedMediaUris) { uri ->
                                SelectedMediaItem(mediaUri = uri, onRemove = { viewModel.removeMediaUri(uri) })
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            cameraExecutor.shutdown()
        }
    }
}

@Composable
private fun SelectedMediaItem(
    mediaUri: String,
    onRemove: () -> Unit
) {
    Box {
        Card(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(8.dp)) {
            Image(
                painter = rememberAsyncImagePainter(mediaUri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()

            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (Uri) -> Unit
) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture?.takePicture(
        outputFileOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { onImageCaptured(it) }
            }
            override fun onError(exception: ImageCaptureException) { // Fixed syntax
                // Handle error
            }
        }
    )
}
private fun captureVideo(
    context: Context,
    videoCapture: VideoCapture<Recorder>?,
    onVideoCaptured: (Uri, Recording) -> Unit
) {
    if (videoCapture == null) return

    // Step 1: Prepare ContentValues for MediaStore (this stores URI, not File)
    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
    }

    // Step 2: Create MediaStoreOutputOptions
    val mediaStoreOutput = MediaStoreOutputOptions.Builder(
        context.contentResolver,
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    ).setContentValues(contentValues).build()

    // Step 3: Declare recording outside to use in lambda
    var recording: Recording? = null

    // Step 4: Start recording
    recording = videoCapture.output
        .prepareRecording(context, mediaStoreOutput)
        .start(ContextCompat.getMainExecutor(context)) { recordEvent ->

            when (recordEvent) {
                is VideoRecordEvent.Finalize -> {
                    val uri = recordEvent.outputResults.outputUri
                    if (uri != Uri.EMPTY) {
                        recording?.let {
                            onVideoCaptured(uri, it)
                        }
                    }
                }
                // Optionally handle Start, Status, Pause, Resume events if needed
            }
        }
}
private fun stopVideoRecording(activeRecording: Recording?) {
    activeRecording?.stop()
}