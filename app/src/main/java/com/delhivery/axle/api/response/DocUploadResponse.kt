package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class DocUploadResponse (
    @SerializedName("manual_verification_required") var manualVerificationRequired: Boolean?,
    @SerializedName("message") var message: String?,
    @SerializedName("verified") var isVerified: Boolean?
)