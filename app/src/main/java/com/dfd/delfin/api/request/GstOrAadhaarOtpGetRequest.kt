package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class GstOrAadhaarOtpGetRequest (
    @SerializedName("verification_type") val verification_type: String,
    @SerializedName("verification_id") val verification_id: String
    )
