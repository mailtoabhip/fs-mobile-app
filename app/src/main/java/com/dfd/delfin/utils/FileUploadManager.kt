package com.dfd.delfin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.data.dispute.ValidationResult
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling file uploads in dispute forms
 */
@Singleton
class FileUploadManager @Inject constructor(
    private val loadboardRepository: LoadboardRepository
) {

    /**
     * Validate a file against allowed types and size constraints
     */
    fun validateFile(
        uri: Uri,
        allowedTypes: List<String>,
        maxSizeMB: Int,
        context: Context,
        validationErrorMessage: String
    ): ValidationResult {
        // Validate file type
        val extension = getFileExtension(uri, context)
        if (!allowedTypes.any { it.equals(extension, ignoreCase = true) }) {
            return ValidationResult(
                isValid = false,
                errorMessage = validationErrorMessage
            )
        }

        // Validate file size
        val fileSizeBytes = getFileSize(uri, context)
        val fileSizeMB = fileSizeBytes / (1024.0 * 1024.0)
        if (fileSizeMB > maxSizeMB) {
            return ValidationResult(
                isValid = false,
                errorMessage = validationErrorMessage
            )
        }

        return ValidationResult(isValid = true)
    }


    /**
     * Get file size in bytes from URI
     */
    fun getFileSize(uri: Uri, context: Context): Long {
        return try {
            // For file:// URIs, read size directly from the File
            if (uri.scheme == "file") {
                val file = File(uri.path!!)
                return if (file.exists()) file.length() else 0L
            }

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                cursor.getLong(sizeIndex)
            } ?: run {
                // Fallback: read from input stream
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.available().toLong()
                } ?: 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get file extension from URI
     */
    fun getFileExtension(uri: Uri, context: Context): String {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            
            // Try to get extension from MIME type
            mimeType?.let {
                when {
                    it.contains("jpeg") || it.contains("jpg") -> "JPG"
                    it.contains("png") -> "PNG"
                    it.contains("pdf") -> "PDF"
                    else -> {
                        // Fallback: get from file name
                        getFileName(uri, context)?.substringAfterLast(".", "")?.uppercase() ?: ""
                    }
                }
            } ?: run {
                // Fallback: get from file name
                getFileName(uri, context)?.substringAfterLast(".", "")?.uppercase() ?: ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get file name from URI
     */
    private fun getFileName(uri: Uri, context: Context): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: uri.lastPathSegment
        } catch (e: Exception) {
            uri.lastPathSegment
        }
    }

    /**
     * Create a thumbnail bitmap for image files
     * Returns null for non-image files
     */
    fun createThumbnail(uri: Uri, context: Context): Bitmap? {
        return try {
            val extension = getFileExtension(uri, context)
            if (extension.uppercase() !in listOf("JPG", "JPEG", "PNG")) {
                return null
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Decode with inJustDecodeBounds to get dimensions
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                
                // Calculate sample size for thumbnail (max 200x200)
                val targetSize = 200
                options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
                options.inJustDecodeBounds = false
                
                // Decode actual bitmap
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate sample size for bitmap decoding
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Create a temporary file from URI for upload
     */
    private fun createTempFileFromUri(uri: Uri, context: Context): File {
        val extension = getFileExtension(uri, context).lowercase()
        val tempFile = File.createTempFile("upload_", ".$extension", context.cacheDir)
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        return tempFile
    }

    /**
     * Format file size for display
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
