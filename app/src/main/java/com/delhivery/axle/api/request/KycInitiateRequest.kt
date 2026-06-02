package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class KycInitiateRequest(
    @SerializedName("bank_code")
    val bankCode: String,
    @SerializedName("kyc_type")
    val kycType: String
)
