package com.dfd.delfin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.dfd.delfin.api.response.FileData
import com.dfd.delfin.api.service.DocumentService
import com.dfd.delfin.utils.constants.FileType
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for secure document upload and download
 * Replaces AWSUtils functionality with new document APIs
 */
@Singleton
class DocumentUtils @Inject constructor(
    private val documentService: DocumentService
) {

    private val compositeDisposable = CompositeDisposable()

    fun test() {
        // Simple test method
    }

    /**
     * Upload single file using secure document API
     * @param file File to upload
     * @param fileType Type of file (IMAGE or PDF)
     * @param docType Document type (e.g., "vendor_pod", "docket", "iv", etc.)
     * @param listener Callback interface for upload progress
     * @param dynamicVariables Optional map of dynamic variables (transactionId, uploadImageName, phoneNumber, docType, proofType)
     */
    fun uploadDocument(
        file: File,
        fileType: FileType,
        docType: String,
        listener: DocumentProgressInterface,
        dynamicVariables: Map<String, String>? = null
    ) {
        uploadDocuments(listOf(file), fileType, docType, listener, dynamicVariables)
    }

    /**
     * Upload multiple files using secure document API
     * @param files List of files to upload
     * @param fileType Type of file (IMAGE or PDF)
     * @param docType Document type (e.g., "vendor_pod", "docket", "iv", etc.)
     * @param listener Callback interface for upload progress
     * @param dynamicVariables Optional map of dynamic variables (transactionId, uploadImageName, phoneNumber, docType, proofType)
     */
    fun uploadDocuments(
        files: List<File>,
        fileType: FileType,
        docType: String,
        listener: DocumentProgressInterface,
        dynamicVariables: Map<String, String>? = null
    ) {
        try {
            val fileParts = files.map { file ->
                val mediaTypeString = getMediaType(fileType)
                val requestFile = RequestBody.create(
                    MediaType.parse(mediaTypeString),
                    file
                )

                // Ensure filename has an appropriate extension to satisfy server validation
                val safeFileName = when {
                    file.name.contains('.') -> file.name
                    mediaTypeString == FileType.IMAGE.value -> file.name + ".jpg"
                    //mediaTypeString == "image/png" -> file.name + ".png"
                    mediaTypeString == FileType.PDF.value -> file.name + ".pdf"
                    //FALLBACK TO "".jpg""
                    else -> ".jpg"
                }

                Log.d("safeFileName===>>>", safeFileName)

                MultipartBody.Part.createFormData("files", safeFileName, requestFile)
            }

            val docTypePart = MultipartBody.Part.createFormData(
                "doc_type", 
                mapDocumentType(docType)
            )

            // Create dynamic_variables part if provided
            val dynamicVariablesPart: MultipartBody.Part? = dynamicVariables?.let { vars ->
                if (vars.isNotEmpty()) {
                    val jsonString = Gson().toJson(vars)
                    MultipartBody.Part.createFormData("dynamic_variables", jsonString)
                } else {
                    null
                }
            }
            Log.d("UploadDocuments", "Request Body -> Files: ${files.map { it.name }}, doc_type: ${mapDocumentType(docType)}, dynamic_variables: ${dynamicVariables?.let { Gson().toJson(it) } ?: "null"}")

            compositeDisposable.add(
                documentService.uploadDocuments(fileParts, docTypePart, dynamicVariablesPart)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { response ->
                            if (response.status == "success") {
                                // Extract downloadUrl from response - first file's downloadUrl
                                // This downloadUrl will be sent to /upload_document API for verification
                                val downloadUrl = response.data?.files?.firstOrNull()?.s3Path
                                if (downloadUrl != null) {
                                    listener.onDocumentSuccess(downloadUrl)
                                } else {
                                    listener.onDocumentFailure("Upload successful but no download URL received")
                                }
                            } else {
                                listener.onDocumentFailure(response.message ?: "Upload failed")
                            }
                        },
                        { error ->
                            Log.e("DocumentUtils", "Upload error: ${error.message}")
                            listener.onDocumentFailure(error.message ?: "Upload failed")
                        }
                    )
            )
        } catch (e: Exception) {
            Log.e("DocumentUtils", "Upload exception: ${e.message}")
            listener.onDocumentFailure(e.message ?: "Upload failed")
        }
    }

    /**
     * List documents by type (Mode 1)
     */
    fun listDocuments(
        docType: String,
        listener: DocumentListInterface
    ) {
        try {
            compositeDisposable.add(
                documentService.listDocuments(mapDocumentType(docType))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { response ->
                            if (response.status == "success") {
                                //listener.onDocumentListSuccess(response.data?.files ?: emptyList())
                            } else {
                                listener.onDocumentListFailure(response.message ?: "List failed")
                            }
                        },
                        { error ->
                            Log.e("DocumentUtils", "List error: ${error.message}")
                            listener.onDocumentListFailure(error.message ?: "List failed")
                        }
                    )
            )
        } catch (e: Exception) {
            Log.e("DocumentUtils", "List exception: ${e.message}")
            listener.onDocumentListFailure(e.message ?: "List failed")
        }
    }

    /**
     * Download document by s3_path (Mode 2)
     * @param s3Path S3 path of the document (e.g., "trips/vendor_pod/docket/docket_1764233770054.jpg")
     * @param listener Callback interface for download result
     */
    fun downloadByS3Path(
        s3Path: String,
        listener: DocumentListInterface
    ) {
        try {
            compositeDisposable.add(
                documentService.listDocuments(s3Path)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { response ->
                            if (response.status == "success") {
                                Log.d("downloadByS3Path", "LOG_1")
                                listener.onDocumentListSuccess(response.data?.files ?: emptyList())
                            } else {
                                Log.d("downloadByS3Path", "LOG_2")
                                listener.onDocumentListFailure(response.message ?: "Download failed")
                            }
                        },
                        { error ->
                            Log.d("downloadByS3Path", "LOG_3")
                            Log.e("DocumentUtils", "Download by s3_path error: ${error.message}")
                            listener.onDocumentListFailure(error.message ?: "Download failed")
                        }
                    )
            )
        } catch (e: Exception) {
            Log.d("downloadByS3Path", "LOG_4")
            Log.e("DocumentUtils", "Download by s3_path exception: ${e.message}")
            listener.onDocumentListFailure(e.message ?: "Download failed")
        }
    }

    fun getPathFromUri(context: Context, uri: Uri): String {
        val file = File(context.cacheDir, "source_${System.currentTimeMillis()}.png")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    fun convertPngToJpg(sourcePath: String, destPath: String, quality: Int = 90): Boolean {

        // Ensure destination path ends with .jpg
        val finalDestPath = if (!destPath.endsWith(".jpg", ignoreCase = true)) {
            "$destPath.jpg"
        } else {
            destPath
        }

        return try {
            val bitmap = BitmapFactory.decodeFile(sourcePath)
                ?: throw IOException("Unable to decode image. The file may be corrupted or unsupported.")

            val jpgBitmap = if (bitmap.hasAlpha()) {
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888).apply {
                    val canvas = Canvas(this)
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                }
            } else {
                bitmap
            }

            FileOutputStream(finalDestPath).use { out ->
                jpgBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Map internal document types to API document types
     */
    private fun mapDocumentType(internalType: String): String {
        return when (internalType.lowercase()) {
            "cin_" -> "cin"
            "udyog_" -> "udyog_aadhaar"
            "shop_" -> "shop_establishment"
            "account_proof_" -> "cancelled_cheque"
            "194c_" -> "section_194C"
            "pan_card" -> "pan"
            "aadhaar_card" -> "aadhaar"
            "gst_card" -> "gst"
            "rc_card" -> "rc"
            "lr_card" -> "lr"
            "driving_license" -> "driving_licence"
            "visiting_card" -> "visiting_card"
            "letterhead" -> "letterhead"
            "passbook" -> "passbook_front_page"
            "loading_note" -> "loading_note"
            "lr_copy" -> "lr_copy"
            "rc_copy" -> "rc_copy"
            else -> internalType.lowercase()
        }
    }

    /**
     * Get media type based on file extension
     */
    private fun getMediaType(fileType: FileType): String {
        return when (fileType) {
            FileType.IMAGE -> FileType.IMAGE.value
            FileType.PDF -> FileType.PDF.value
        }
    }

    /**
     * Clean up resources
     */
    fun dispose() {
        compositeDisposable.clear()
    }

    /**
     * Document upload result interface
     * @param downloadUrl The download URL returned from the upload API (to be used for verification)
     */
    interface DocumentProgressInterface {
        fun onDocumentSuccess(downloadUrl: String)
        fun onDocumentFailure(error: String)
    }

    /**
     * Document download list result interface
     */
    interface DocumentListInterface {
        fun onDocumentListSuccess(files: List<FileData>)
        fun onDocumentListFailure(error: String)
    }
}
