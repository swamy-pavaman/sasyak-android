package com.kapilagro.sasyak.data.api

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import com.kapilagro.sasyak.domain.models.ApiResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.RandomAccessFile
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.min

/**
 * MultipartVideoUploadService
 *
 * Uploads large files (video/images) to the backend using multipart upload.
 * Uses RandomAccessFile so we can jump directly to any part and resume reliably.
 *
 * Assumptions:
 * - apiService.startMultipartUpload(...) returns an object that contains uploadId.
 * - apiService.getPresignedUrl(uploadId, fileName, folder, partNumber) returns a single presigned URL for that part.
 * - apiService.completeMultipartUpload(...) finalizes the upload and returns finalUrl on success.
 */
class MultipartVideoUploadService @Inject constructor(
    private val apiService: ApiService,
    @Named("uploadClient") private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences("UploadState", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private fun getMimeTypeFromFile(file: File): String {
        val extension = file.extension.lowercase(Locale.ROOT)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    /**
     * Uploads the given file in multipart chunks and completes the upload.
     * Returns ApiResponse.Success(finalUrl) on success, or ApiResponse.Error on failure.
     */
    suspend fun uploadFileInChunks(file: File, folder: String, chunkSizeBytes: Int = 5 * 1024 * 1024): ApiResponse<String> =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Starting multipart upload for file=${file.name}, folder=$folder")
            if (!file.exists()) {
                return@withContext ApiResponse.Error("File does not exist: ${file.absolutePath}")
            }

            val fileKey = "$folder/${file.name}"
            val chunkSize = chunkSizeBytes.coerceAtLeast(1024) // minimum sane size
            val partETags = mutableListOf<MultipartETag>()

            try {
                // Step 1: restore saved upload state if any
                var uploadId = prefs.getString("$fileKey:uploadId", null)
                prefs.getString("$fileKey:parts", null)?.let { jsonStr ->
                    try {
                        val saved = json.decodeFromString<List<MultipartETag>>(jsonStr)
                        partETags.addAll(saved)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to decode saved parts for $fileKey: ${e.message}. Ignoring saved parts.")
                    }
                }

                // If no uploadId, initiate a new multipart upload
                if (uploadId.isNullOrBlank()) {
                    val startResp = apiService.startMultipartUpload(MultipartUploadInitRequest(file.name, folder))
                    if (!startResp.isSuccessful || startResp.body()?.uploadId == null) {
                        return@withContext ApiResponse.Error("Failed to start multipart upload")
                    }
                    uploadId = startResp.body()!!.uploadId
                    prefs.edit().putString("$fileKey:uploadId", uploadId).apply()
                }

                val totalParts = ((file.length() + chunkSize - 1) / chunkSize).toInt()
                Log.d(TAG, "uploadId=$uploadId totalParts=$totalParts alreadyUploadedParts=${partETags.size}")

                // Start from next part after highest saved partNumber
                var partNumber = (partETags.maxByOrNull { it.partNumber }?.partNumber ?: 0) + 1
                if (partNumber < 1) partNumber = 1

                RandomAccessFile(file, "r").use { raf ->
                    while (partNumber <= totalParts) {
                        Log.d(TAG, "Preparing upload for part $partNumber / $totalParts")

                        // If already uploaded (shouldn't happen because we set start partNumber), skip
                        if (partETags.any { it.partNumber == partNumber }) {
                            Log.d(TAG, "Part $partNumber already uploaded; skipping")
                            partNumber++
                            continue
                        }

                        // Request presigned URL for this part from backend
                        val presignedResp = apiService.getPresignedUrl(
                            uploadId = uploadId,
                            fileName = file.name,
                            folder = folder,
                            partNumber = partNumber
                        )
                        if (!presignedResp.isSuccessful || presignedResp.body()?.url == null) {
                            return@withContext ApiResponse.Error("Failed to fetch presigned URL for part $partNumber")
                        }
                        val uploadUrl = presignedResp.body()!!.url

                        // Seek and read the exact bytes for this part
                        val offset = (partNumber - 1).toLong() * chunkSize
                        raf.seek(offset)
                        val remaining = (file.length() - offset).coerceAtLeast(0)
                        val bytesToRead = min(remaining, chunkSize.toLong()).toInt()
                        val buffer = ByteArray(bytesToRead)
                        raf.readFully(buffer)

                        val mimeType = getMimeTypeFromFile(file)
                        val requestBody = buffer.toRequestBody("application/octet-stream".toMediaType())

                        val request = Request.Builder()
                            .url(uploadUrl)
                            .put(requestBody)
                            .addHeader("Content-Type", mimeType)
                            .build()

                        val response = okHttpClient.newCall(request).execute()
                        if (!response.isSuccessful) {
                            Log.e(TAG, "Upload failed for part $partNumber: ${response.code} ${response.message}")
                            return@withContext ApiResponse.Error("Chunk $partNumber failed: ${response.message}")
                        }

                        val eTagHeader = response.header("ETag")
                        if (eTagHeader.isNullOrBlank()) {
                            Log.e(TAG, "Missing ETag for part $partNumber")
                            return@withContext ApiResponse.Error("Missing ETag for part $partNumber")
                        }

                        val cleanETag = eTagHeader.replace("\"", "")
                        val newETag = MultipartETag(partNumber, cleanETag)
                        partETags.add(newETag)

                        // Persist parts after each success for resume
                        prefs.edit().putString("$fileKey:parts", json.encodeToString(partETags)).apply()

                        Log.d(TAG, "Uploaded part $partNumber, eTag=$cleanETag")
                        partNumber++
                    }
                }

                // Step 4: Complete multipart upload
                val completeRequest = MultipartCompleteRequest(
                    uploadId = uploadId!!,
                    fileName = file.name,
                    folder = folder,
                    parts = partETags
                )
                val completeResp = apiService.completeMultipartUpload(completeRequest)
                if (completeResp.isSuccessful && completeResp.body()?.success == true) {
                    // clear saved state
                    prefs.edit().remove("$fileKey:uploadId").remove("$fileKey:parts").apply()
                    val finalUrl = completeResp.body()!!.url
                    Log.d(TAG, "Multipart upload completed successfully. finalUrl=$finalUrl")
                    return@withContext ApiResponse.Success(finalUrl)
                } else {
                    Log.e(TAG, "Failed to complete multipart upload: ${completeResp.message()}")
                    return@withContext ApiResponse.Error("Failed to complete upload")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in multipart upload: ${e.message}", e)
                return@withContext ApiResponse.Error(e.message ?: "Unexpected error during multipart upload")
            }
        }

    suspend fun abortUpload(file: File, folder: String) = withContext(Dispatchers.IO) {
        val fileKey = "$folder/${file.name}"
        val uploadId = prefs.getString("$fileKey:uploadId", null) ?: return@withContext
        try {
            apiService.abortMultipartUpload(MultipartAbortRequest(uploadId, file.name, folder))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to abort multipart upload for $fileKey: ${e.message}")
        } finally {
            prefs.edit().remove("$fileKey:uploadId").remove("$fileKey:parts").apply()
        }
    }

    companion object {
        private const val TAG = "MultipartVideoUploadSvc"
    }
}

/**
 * Data classes used for the multipart flow. Keep @Serializable so we can persist part lists.
 * (Make sure your API DTOs match these shapes or adapt as needed.)
 */
@Serializable
data class MultipartUploadInitRequest(
    val fileName: String,
    val folder: String
)

@Serializable
data class MultipartUploadInitResponse(
    val uploadId: String
)

@Serializable
data class PresignedUrlRequestMultipart(
    val uploadId: String,
    val fileName: String,
    val folder: String,
    val partNumber: Int
)

@Serializable
data class PresignedUrlResponseMultipart(
    val url: String
)

@Serializable
data class MultipartETag(
    val partNumber: Int,
    val etag: String
)

@Serializable
data class MultipartCompleteRequest(
    val uploadId: String,
    val fileName: String,
    val folder: String,
    val parts: List<MultipartETag>
)

@Serializable
data class MultipartCompleteResponse(
    val success: Boolean,
    val url: String
)

@Serializable
data class MultipartAbortRequest(
    val uploadId: String,
    val fileName: String,
    val folder: String
)
