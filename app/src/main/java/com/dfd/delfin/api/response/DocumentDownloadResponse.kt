package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for document download/list API
 */
data class DocumentDownloadResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: DocumentDownloadDataV2?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("code")
    val code: Int?
)

data class DocumentDownloadData(
    @SerializedName("files")
    val files: List<DocumentFile>?,
    @SerializedName("count")
    val count: Int?,
    @SerializedName("doc_type")
    val docType: String?
)

data class DocumentFile(
    @SerializedName("filename")
    val filename: String,
    @SerializedName("doc_type")
    val docType: String?,
    @SerializedName("s3_path")
    val s3Path: String?,
    @SerializedName("download_url")
    val downloadUrl: String,
    @SerializedName("expires_in")
    val expiresIn: Int?
)

data class DocumentDownloadResponseV2(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: DocumentDownloadDataV2?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("code")
    val code: Int?
)

data class DocumentDownloadDataV2(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("files")
    val files: List<FileData>,

    @SerializedName("count")
    val count: Int,

    @SerializedName("error_message")
    val errorMessage: String?
)

data class FileData(
    @SerializedName("filename")
    val filename: String,

    @SerializedName("s3_path")
    val s3Path: String,

    @SerializedName("download_url")
    val downloadUrl: String,

    @SerializedName("expires_in")
    val expiresIn: Int
)
