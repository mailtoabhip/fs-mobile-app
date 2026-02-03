package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class FastagLeadRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("vehicle_count") val vehicleCount: Int,
    @SerializedName("location") val location: String,
    @SerializedName("source") val source: String = "Axle",
    @SerializedName("vrn") val vrn: String? = null
)
