package com.kapilagro.sasyak.presentation.scanner

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sasyak.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri.asStateFlow()

    fun processCapturedImage(bitmap: Bitmap, context: Context) {
        _scanState.value = ScanState.Analyzing
        viewModelScope.launch(ioDispatcher) {
            try {
                // Save bitmap to file
                val file = saveBitmapToFile(bitmap, context)
                _capturedImageUri.value = Uri.fromFile(file)

                // Simulate analysis delay
                kotlinx.coroutines.delay(2000)

                // For demo purposes, return a mock result
                _scanState.value = ScanState.Success(
                    diseaseName = "Powdery Mildew",
                    confidence = 89.5f,
                    description = "Powdery mildew is a fungal disease that affects a wide range of plants. It appears as a white to gray powdery coating on leaf surfaces, stems, and sometimes fruit.",
                    recommendations = listOf(
                        "Apply fungicide specifically designed for powdery mildew",
                        "Improve air circulation by pruning dense foliage",
                        "Avoid overhead watering to keep leaves dry",
                        "Remove and destroy infected plant parts"
                    )
                )
            } catch (e: Exception) {
                _scanState.value = ScanState.Error("Failed to process image: ${e.message}")
            }
        }
    }

    fun processGalleryImage(uri: Uri) {
        _capturedImageUri.value = uri
        _scanState.value = ScanState.Analyzing
        viewModelScope.launch(ioDispatcher) {
            try {
                // Simulate analysis delay
                kotlinx.coroutines.delay(2000)

                // For demo purposes, return a mock result
                _scanState.value = ScanState.Success(
                    diseaseName = "Leaf Rust",
                    confidence = 76.2f,
                    description = "Leaf rust is a fungal disease characterized by rusty-orange to reddish-brown pustules on leaves. It can cause premature leaf drop and reduce plant vigor.",
                    recommendations = listOf(
                        "Apply appropriate fungicide at first sign of infection",
                        "Avoid watering late in the day to reduce leaf wetness",
                        "Remove infected leaves and destroy them",
                        "Increase spacing between plants to improve air circulation"
                    )
                )
            } catch (e: Exception) {
                _scanState.value = ScanState.Error("Failed to process image: ${e.message}")
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
        val file = File(context.cacheDir, "scanned_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    fun resetScanState() {
        _scanState.value = ScanState.Idle
        _capturedImageUri.value = null
    }

    sealed class ScanState {
        object Idle : ScanState()
        object Analyzing : ScanState()
        data class Success(
            val diseaseName: String,
            val confidence: Float,
            val description: String,
            val recommendations: List<String>
        ) : ScanState()
        data class Error(val message: String) : ScanState()
    }
}