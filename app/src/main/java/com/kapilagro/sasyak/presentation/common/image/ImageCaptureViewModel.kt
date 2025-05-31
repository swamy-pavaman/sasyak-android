package com.kapilagro.sasyak.presentation.common.image

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

class ImageCaptureViewModel @Inject constructor() : ViewModel() {

    private val _selectedImages = MutableStateFlow<List<File>>(emptyList())
    val selectedImages: StateFlow<List<File>> = _selectedImages.asStateFlow()

    fun addImage(imageFile: File) {
        val currentImages = _selectedImages.value.toMutableList()
        if (currentImages.size < 5) {
            currentImages.add(imageFile)
            _selectedImages.value = currentImages
        }
    }

    fun removeImage(imageFile: File) {
        val currentImages = _selectedImages.value.toMutableList()
        currentImages.remove(imageFile)
        _selectedImages.value = currentImages
    }

    fun clearImages() {
        _selectedImages.value = emptyList()
    }
}