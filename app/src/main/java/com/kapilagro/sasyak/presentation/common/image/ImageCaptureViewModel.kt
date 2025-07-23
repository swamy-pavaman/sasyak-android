package com.kapilagro.sasyak.presentation.common.image

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

class ImageCaptureViewModel @Inject constructor() : ViewModel() {

//    private val _selectedMediaUris = MutableStateFlow<List<String>>(emptyList())
//    val selectedMediaUris: StateFlow<List<String>> = _selectedMediaUris.asStateFlow()

    private val _selectedMediaUris = MutableStateFlow<List<String>>(emptyList())
    val selectedMediaUris: StateFlow<List<String>> = _selectedMediaUris.asStateFlow()

    fun addMediaUri(uri: String) {
        if (_selectedMediaUris.value.size < 10) {
            _selectedMediaUris.value = _selectedMediaUris.value + uri
        }
    }

    fun removeMediaUri(uri: String) {
        _selectedMediaUris.value = _selectedMediaUris.value - uri
    }

//    fun addMediaUri(uri: String) {
//        val currentUris = _selectedMediaUris.value.toMutableList()
//        if (currentUris.size < 5) { // Retain the max limit of 5 from original ViewModel
//            currentUris.add(uri)
//            _selectedMediaUris.value = currentUris
//        }
//    }
//
//    fun removeMediaUri(uri: String) {
//        val currentUris = _selectedMediaUris.value.toMutableList()
//        currentUris.remove(uri)
//        _selectedMediaUris.value = currentUris
//    }

    fun clearMediaUris() {
        _selectedMediaUris.value = emptyList()
    }

    fun getSelectedFilePaths(context: Context): List<String> {
        return _selectedMediaUris.value.mapNotNull { uriString ->
            try {
                val uri = Uri.parse(uriString)
                when (uri.scheme) {
                    "file" -> uri.path
                    "content" -> copyUriToFile(context, uri)?.absolutePath
                    else -> null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun copyUriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "media_${System.currentTimeMillis()}")
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    companion object {
        /**
         * ★★★ NEW: Robust function to copy a content URI to a cached file with the correct extension.
         * This can be used anywhere you need to convert a URI to a temporary File.
         */
        fun copyUriToCachedFile(context: Context, uri: Uri): File? {
            return try {
                val contentResolver = context.contentResolver
                // 1. Get the MIME type (e.g., "image/jpeg") from the URI
                val mimeType = contentResolver.getType(uri)
                // 2. Get the corresponding extension (e.g., "jpg")
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"

                // 3. Create a file in the cache directory with the correct name and extension
                val tempFile = File(context.cacheDir, "media_${System.currentTimeMillis()}.$extension")

                // 4. Copy the content from the URI's input stream to the new file
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                tempFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


    }





}