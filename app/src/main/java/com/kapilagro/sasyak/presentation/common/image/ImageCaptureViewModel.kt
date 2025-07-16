package com.kapilagro.sasyak.presentation.common.image

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ImageCaptureViewModel @Inject constructor() : ViewModel() {

    private val _selectedMediaUris = MutableStateFlow<List<String>>(emptyList())
    val selectedMediaUris: StateFlow<List<String>> = _selectedMediaUris.asStateFlow()

    fun addMediaUri(uri: String) {
        val currentUris = _selectedMediaUris.value.toMutableList()
        if (currentUris.size < 5) { // Retain the max limit of 5 from original ViewModel
            currentUris.add(uri)
            _selectedMediaUris.value = currentUris
        }
    }

    fun removeMediaUri(uri: String) {
        val currentUris = _selectedMediaUris.value.toMutableList()
        currentUris.remove(uri)
        _selectedMediaUris.value = currentUris
    }

    fun clearMediaUris() {
        _selectedMediaUris.value = emptyList()
    }

    fun getSelectedFilePaths(): List<String> {
        return _selectedMediaUris.value.mapNotNull { uriString ->
            try {
                // Convert URI to actual file path
                // This is a simplified version - you might need more robust URI to file conversion
                val uri = Uri.parse(uriString)
                when (uri.scheme) {
                    "file" -> uri.path
                    "content" -> {
                        // Handle content URIs - you might need to copy to app directory
                        // For now, returning the URI string itself
                        uriString
                    }
                    else -> uriString
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}