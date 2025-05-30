package com.kapilagro.sasyak.presentation.common.image

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCaptureScreen(
    folder: String,
    maxImages: Int = 5,
    onImagesSelected: (List<File>) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ImageCaptureViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedImages by viewModel.selectedImages.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        uris.take(maxImages - selectedImages.size).forEach { uri ->
            val file = createImageFileFromUri(context, uri)
            file?.let { viewModel.addImage(it) }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture Images") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedImages.isNotEmpty()) {
                        TextButton(
                            onClick = { onImagesSelected(selectedImages) },
                            enabled = selectedImages.isNotEmpty()
                        ) {
                            Text("Done (${selectedImages.size})")
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
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
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
                                if (selectedImages.size < maxImages) {
                                    galleryLauncher.launch("image/*")
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = "Gallery")
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        FloatingActionButton(
                            onClick = {
                                if (selectedImages.size < maxImages) {
                                    captureImage(context, imageCapture) { file ->
                                        viewModel.addImage(file)
                                    }
                                }
                            },
                            modifier = Modifier.size(72.dp),
                            containerColor = if (selectedImages.size >= maxImages) Color.Gray else MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Camera, contentDescription = "Capture", modifier = Modifier.size(32.dp))
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
                        Text("Camera permission required")
                        Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            if (selectedImages.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Selected Images (${selectedImages.size}/$maxImages)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedImages) { imageFile ->
                                SelectedImageItem(imageFile = imageFile, onRemove = { viewModel.removeImage(imageFile) })
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }
}

@Composable
private fun SelectedImageItem(
    imageFile: File,
    onRemove: () -> Unit
) {
    Box {
        Card(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(8.dp)) {
            Image(
                painter = rememberAsyncImagePainter(imageFile),
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
    context: android.content.Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (File) -> Unit
) {
    val imageFile = File(
        context.getExternalFilesDir(null),
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

    imageCapture?.takePicture(
        outputFileOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(imageFile)
            }
            override fun onError(exception: ImageCaptureException) {
                // Handle error
            }
        }
    )
}

private fun createImageFileFromUri(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "gallery_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(null), fileName)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}