package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class DocumentUploadResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("data")
    val data: DocumentUploadData?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("status")
    val status: String
)

data class DocumentUploadData(
    @SerializedName("count")
    val count: Int,

    @SerializedName("error_message")
    val errorMessage: String?,

    @SerializedName("files")
    val files: List<UploadedFile>,

    @SerializedName("success")
    val success: Boolean
)

data class UploadedFile(
    @SerializedName("download_url")
    val downloadUrl: String?,

    @SerializedName("expires_in")
    val expiresIn: Int?,

    @SerializedName("filename")
    val filename: String,

    @SerializedName("s3_path")
    val s3Path: String
)
