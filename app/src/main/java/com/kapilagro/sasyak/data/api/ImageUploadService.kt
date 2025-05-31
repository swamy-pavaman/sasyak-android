package com.kapilagro.sasyak.data.api

import com.kapilagro.sasyak.domain.models.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.File
import javax.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Request

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
    suspend fun uploadImages(imageFiles: List<File>, folder: String): ApiResponse<List<String>> = withContext(Dispatchers.IO) {
        try {
            val fileNames = imageFiles.map { it.name }
            val request = PresignedUrlRequest(
                fileNames = fileNames,
                expiryHours = 1,
                folder = folder
            )

            val response = apiService.getPresignedUrls(request)
            if (!response.isSuccessful) {
                return@withContext ApiResponse.Error("Failed to get presigned URLs: ${response.message()}")
            }

            val presignedResponse = response.body() ?: return@withContext ApiResponse.Error("Empty response")
            if (!presignedResponse.success) {
                return@withContext ApiResponse.Error(presignedResponse.message)
            }

            val uploadedUrls = mutableListOf<String>()
            for (file in imageFiles) {
                val presignedUrl = presignedResponse.presignedUrls[file.name]
                    ?: return@withContext ApiResponse.Error("No presigned URL for ${file.name}")

                val requestBody = file.asRequestBody("image/jpeg".toMediaType())
                val request = Request.Builder()
                    .url(presignedUrl)
                    .put(requestBody)
                    .addHeader("Content-Type", "image/jpeg")
                    .build()

                val uploadResponse = okHttpClient.newCall(request).execute()
                if (uploadResponse.isSuccessful) {
                    uploadedUrls.add(extractCleanUrl(presignedUrl))
                } else {
                    return@withContext ApiResponse.Error("Failed to upload ${file.name}: ${uploadResponse.message}")
                }
            }
            ApiResponse.Success(uploadedUrls)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error during upload")
        }
    }

    private fun extractCleanUrl(presignedUrl: String): String {
        return presignedUrl.substringBefore("?")
    }
}