package com.kapilagro.sasyak.presentation.common.image

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCaptureScreen(
    folder: String,
    maxMedia: Int = 10,
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

    // Use remember for objects that should persist across recompositions
    val previewView = remember { PreviewView(context) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val videoCapture: VideoCapture<Recorder> = remember {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .setExecutor(cameraExecutor)
            .build()
        VideoCapture.withOutput(recorder)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        hasCameraPermission = permissions.getOrDefault(Manifest.permission.CAMERA, false) &&
                permissions.getOrDefault(Manifest.permission.RECORD_AUDIO, false)
    }

    // This effect handles camera setup and binding. It runs once when permissions are granted.
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val cameraProvider = ProcessCameraProvider.getInstance(context).await()
            bindCameraUseCases(cameraProvider, lifecycleOwner, previewView, imageCapture, videoCapture)
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture Media") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    if (selectedMediaUris.isNotEmpty()) {
                        TextButton(onClick = { onMediaSelected(selectedMediaUris) }) {
                            Text("Done (${selectedMediaUris.size})")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (hasCameraPermission) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

                    CameraControls(
                        isRecording = isRecording,
                        mediaCount = selectedMediaUris.size,
                        maxMedia = maxMedia,
                        imageCapture = imageCapture,
                        videoCapture = videoCapture,
                        onMediaSaved = { uri -> viewModel.addMediaUri(uri.toString()) },
                        onIsRecordingChange = { isRecording = it }
                    )
                }
            } else {
                // Permission rationale UI
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera and audio permissions are required.")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }) {
                            Text("Grant Permissions")
                        }
                    }
                }
            }

            // Selected Media Preview
            if (selectedMediaUris.isNotEmpty()) {
                SelectedMediaRow(
                    mediaUris = selectedMediaUris,
                    maxMedia = maxMedia,
                    onRemove = { viewModel.removeMediaUri(it) }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

// Helper function to keep the main composable clean
private fun bindCameraUseCases(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    videoCapture: VideoCapture<Recorder>
) {
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val preview = Preview.Builder()
        .setTargetRotation(previewView.display.rotation)
        .build()
        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

    imageCapture.targetRotation = previewView.display.rotation
    videoCapture.targetRotation = previewView.display.rotation

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture)
    } catch (e: Exception) {
        Log.e("ImageCaptureScreen", "Use case binding failed", e)
    }
}

@Composable
private fun BoxScope.CameraControls(
    isRecording: Boolean,
    mediaCount: Int,
    maxMedia: Int,
    imageCapture: ImageCapture,
    videoCapture: VideoCapture<Recorder>,
    onMediaSaved: (Uri) -> Unit,
    onIsRecordingChange: (Boolean) -> Unit
) {
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxMedia)
    ) { uris: List<Uri> ->
        val remainingSlots = maxMedia - mediaCount
        uris.take(remainingSlots).forEach(onMediaSaved)
    }

    Row(
        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val buttonsEnabled = mediaCount < maxMedia

        // Gallery Button
        FloatingActionButton(
            onClick = { if (buttonsEnabled) galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
            containerColor = if (buttonsEnabled) MaterialTheme.colorScheme.secondary else Color.Gray
        ) { Icon(Icons.Default.PhotoLibrary, "Gallery") }

        // Image Capture Button
        FloatingActionButton(
            onClick = { if (buttonsEnabled && !isRecording) captureImage(context, imageCapture, onMediaSaved) },
            modifier = Modifier.size(72.dp),
            containerColor = if (buttonsEnabled && !isRecording) MaterialTheme.colorScheme.primary else Color.Gray
        ) { Icon(Icons.Default.Camera, "Capture Image", modifier = Modifier.size(36.dp)) }

        // Video Capture Button
        FloatingActionButton(
            onClick = {
                if (!buttonsEnabled) return@FloatingActionButton

                if (isRecording) {
                    activeRecording?.stop()
                    activeRecording = null
                    onIsRecordingChange(false)
                } else {
                    onIsRecordingChange(true)
                    activeRecording = startRecording(context, videoCapture) { uri ->
                        if (uri != null) {
                            onMediaSaved(uri)
                        }
                        onIsRecordingChange(false)
                        activeRecording = null
                    }
                }
            },
            containerColor = if (buttonsEnabled) (if (isRecording) Color.Red else MaterialTheme.colorScheme.primary) else Color.Gray
        ) {
            Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Videocam, if (isRecording) "Stop" else "Record")
        }
    }
}

private fun startRecording(context: Context, videoCapture: VideoCapture<Recorder>, onResult: (Uri?) -> Unit): Recording {
    val name = "video_${System.currentTimeMillis()}.mp4"
    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, name)
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SasyakApp")
    }
    val mediaStoreOutput = MediaStoreOutputOptions.Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        .setContentValues(contentValues).build()

    return videoCapture.output
        .prepareRecording(context, mediaStoreOutput)
        .apply { if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) withAudioEnabled() }
        .start(ContextCompat.getMainExecutor(context)) { event ->
            if (event is VideoRecordEvent.Finalize) {
                onResult(if (event.hasError()) null else event.outputResults.outputUri)
            }
        }
}

private fun captureImage(context: Context, imageCapture: ImageCapture, onResult: (Uri) -> Unit) {
    val name = "image_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SasyakApp")
    }
    val outputOptions = ImageCapture.OutputFileOptions.Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build()

    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            output.savedUri?.let(onResult)
        }
        override fun onError(exc: ImageCaptureException) {
            Log.e("captureImage", "Image capture failed: ${exc.message}", exc)
        }
    })
}


@Composable
private fun SelectedMediaRow(mediaUris: List<String>, maxMedia: Int, onRemove: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Selected Media (${mediaUris.size}/$maxMedia)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mediaUris) { uri ->
                    SelectedMediaItem(mediaUri = uri, onRemove = { onRemove(uri) })
                }
            }
        }
    }
}

@Composable
private fun SelectedMediaItem(mediaUri: String, onRemove: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Card(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(8.dp)) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(Uri.parse(mediaUri)).crossfade(true).build()
                ),
                contentDescription = "Selected media",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp).padding(2.dp)) {
            Icon(Icons.Default.Close, "Remove", tint = Color.White)
        }
    }
}