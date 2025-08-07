package com.kapilagro.sasyak.data.api

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.kapilagro.sasyak.domain.models.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

data class PresignedUrlRequest(
    val fileNames: List<String>,
    val expiryHours: Int,
    val folder: String
)

data class PresignedUrlResponse(
    val presignedUrls: Map<String, String>,
    val folder: String,
    val expiryHours: Int,
    val method: String,
    val success: Boolean,
    val count: Int,
    val message: String
)

class ImageUploadService @Inject constructor(
    private val apiService: ApiService,
    private val okHttpClient: OkHttpClient
) {

    private fun getMimeTypeFromFile(file: File): String {
        val extension = file.extension.lowercase(Locale.ROOT)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    suspend fun uploadFilek(context: Context,uris: List<Uri>, folder: String): ApiResponse<List<String>> = withContext(Dispatchers.IO) {
        val files = uris.mapNotNull { uriToFile(context, it) }
        return@withContext uploadFiles(files, folder)
    }
    suspend fun uploadFiles(files: List<File>, folder: String): ApiResponse<List<String>> = withContext(Dispatchers.IO) {
        try {
            // This is now correct, as File objects will have names with extensions (e.g., "media_123.jpg")
            val fileNames = files.map { it.name }

            val request = PresignedUrlRequest(
                fileNames = fileNames,
                expiryHours = 1,
                folder = folder
            )

            val response = apiService.getPresignedUrls(request)
            if (!response.isSuccessful) {
                Log.e("ImageUploadService", "Presigned URL errorBody: ${response.errorBody()?.string()}")
                return@withContext ApiResponse.Error("Failed to get presigned URLs: ${response.message()}")
            }

            val presignedResponse = response.body() ?: return@withContext ApiResponse.Error("Empty response")
            if (!presignedResponse.success) {
                return@withContext ApiResponse.Error(presignedResponse.message)
            }

            val uploadedUrls = mutableListOf<String>()
            for (file in files) {
                val presignedUrl = presignedResponse.presignedUrls[file.name]
                    ?: return@withContext ApiResponse.Error("No presigned URL for ${file.name}")

                // ★★★ FIX: Dynamically determine MIME type instead of hardcoding "image/jpeg".
                val mimeType = getMimeTypeFromFile(file)
                val requestBody = file.asRequestBody(mimeType.toMediaType())

                val putRequest = Request.Builder()
                    .url(presignedUrl)
                    .put(requestBody)
                    .addHeader("Content-Type", mimeType) // This MUST match the type used for signing.
                    .build()

                val uploadResponse = okHttpClient.newCall(putRequest).execute()
                if (uploadResponse.isSuccessful) {
                    Log.d("ImageUploadService", "uploaded succefully with oresigned url: $presignedUrl")
                    uploadedUrls.add(extractCleanUrl(presignedUrl))
                    Log.d("ImageUploadService", "Upload successful after presginerfdurl extracted presignedUrl "+extractCleanUrl(presignedUrl))
                } else {
                    Log.e("ImageUploadService", "Upload failed for ${file.name}. Code: ${uploadResponse.code}. Message: ${uploadResponse.message}.")
                    return@withContext ApiResponse.Error("Failed to upload ${file.name}: ${uploadResponse.message}")
                }
            }
            ApiResponse.Success(uploadedUrls)
        } catch (e: Exception) {
            Log.e("ImageUploadService", "Exception during upload", e)
            ApiResponse.Error(e.message ?: "Unknown error during upload")
        }
    }

    private fun extractCleanUrl(presignedUrl: String): String {
        return presignedUrl.substringBefore("?")
    }
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", null, context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}