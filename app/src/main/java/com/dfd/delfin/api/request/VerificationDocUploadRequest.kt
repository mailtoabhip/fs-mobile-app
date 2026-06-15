package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class VerificationDocUploadRequest(
    @SerializedName("verification_id") var verificationId: String? = null,
    @SerializedName("document_type") var proofDocumentType: String? = null,
    @SerializedName("document_urls") var documentUrls: List<String>? = null
    )
