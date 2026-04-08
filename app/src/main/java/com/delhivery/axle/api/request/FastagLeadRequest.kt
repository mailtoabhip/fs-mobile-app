package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class FastagLeadRequest(
    @SerializedName("vehicle_count") val vehicleCount: Int?,
    @SerializedName("location") val location: String?=null,
    @SerializedName("source") val source: String? = "Axle",
    @SerializedName("vrn") val vrn: String? = null
)
