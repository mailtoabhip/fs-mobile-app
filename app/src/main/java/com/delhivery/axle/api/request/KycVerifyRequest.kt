package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class KycVerifyRequest(
    @SerializedName("journey_id")
    val journeyId: String,
    @SerializedName("otp")
    val otp: String,
    @SerializedName("bank_code")
    val bankCode: String,
    @SerializedName("kyc_type")
    val kycType: String
)
