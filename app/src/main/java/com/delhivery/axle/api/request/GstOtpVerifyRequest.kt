package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class GstOtpVerifyRequest (
        @SerializedName("verification_type") val verification_type: String,
        @SerializedName("verification_id") val verification_id: String,
        @SerializedName("otp") val otp: String
)
