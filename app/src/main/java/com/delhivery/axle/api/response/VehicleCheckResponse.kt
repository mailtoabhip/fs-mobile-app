package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class VehicleCheckResponse(
    @SerializedName("vehicle_number")
    val vehicleNumber: String,
    @SerializedName("is_eligible")
    val isEligible: Boolean,
    @SerializedName("is_hotlisted")
    val isHotlisted: Boolean,
    @SerializedName("issuer_phone")
    val issuerPhone: String?,
    @SerializedName("message")
    val message: String?
)
