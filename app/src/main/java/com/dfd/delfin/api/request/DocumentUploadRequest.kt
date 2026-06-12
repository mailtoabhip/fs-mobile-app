package com.dfd.delfin.api.request

import okhttp3.MultipartBody
import retrofit2.http.Part

/**
 * Request model for document upload API
 * This is used internally for multipart form data
 */
data class DocumentUploadRequest(
    @Part val files: List<MultipartBody.Part>,
    @Part val docType: MultipartBody.Part
)
