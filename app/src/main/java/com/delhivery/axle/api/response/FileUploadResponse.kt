package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for file upload
 */
data class FileUploadResponse(
    @SerializedName("fileUrl")
    val fileUrl: String,
    
    @SerializedName("fileName")
    val fileName: String
)
