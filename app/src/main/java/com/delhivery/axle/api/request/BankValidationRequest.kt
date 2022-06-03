package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class BankValidationRequest (
    @SerializedName("account_number") val accountNumber: String,
    @SerializedName("ifsc_code") val ifscCode: String
)