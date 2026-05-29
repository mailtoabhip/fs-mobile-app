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
    @SerializedName("balance")
    val balance: String?,
    @SerializedName("provider")
    val provider: String?,
    @SerializedName("vehicle_class")
    val vehicleClass: String?,
    @SerializedName("vehicle_class_display")
    val vehicleClassDisplay: String?,
    @SerializedName("tag_color")
    val tagColor: String?,
    @SerializedName("vehicle_type")
    val vehicleType: String?,
    @SerializedName("message")
    val message: String?
)
