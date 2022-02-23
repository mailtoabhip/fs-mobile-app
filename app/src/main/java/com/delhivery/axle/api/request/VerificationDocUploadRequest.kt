package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class VerificationDocUploadRequest(
    @SerializedName("verification_id") var verificationId: String? = null,
    @SerializedName("document_type") var proofDocumentType: String? = null,
    @SerializedName("document_urls") var documentUrls: List<String>? = null,
    @SerializedName("cin_number") var cinNumber: String? = null,
    @SerializedName("udyog_aadhaar_number") var udyogAadhaarNumber: String? = null,
    @SerializedName("shop_establishment_number") var shopEstablishmentNumber: String? = null
    )
