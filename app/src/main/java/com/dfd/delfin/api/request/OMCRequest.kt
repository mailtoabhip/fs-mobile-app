package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class OMCRequest (
    @SerializedName("mobile_number") var mobileNumber: String,
    @SerializedName("omc_type") var omcType: String,
    @SerializedName("trip_id") var triId: String,
    @SerializedName("amount") var amount: Int =0
)
