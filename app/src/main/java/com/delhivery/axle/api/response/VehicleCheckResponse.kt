package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class VehicleCheckResponse(
    @SerializedName("eligible")
    val eligible: Boolean,
    @SerializedName("status")
    val status: String,
    @SerializedName("vrn")
    val vrn: String,
    @SerializedName("npci_vehicle_class")
    val npciVehicleClass: String?,
    @SerializedName("card_stage")
    val cardStage: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("action_label")
    val actionLabel: String?,
    @SerializedName("vehicle_class")
    val vehicleClass: VehicleClassDetail?
)

data class VehicleClassDetail(
    @SerializedName("vehicle_class")
    val vehicleClass: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("weight_range")
    val weightRange: String,
    @SerializedName("color_code")
    val colorCode: String,
    @SerializedName("vehicle_types")
    val vehicleTypes: List<String>,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("sort_order")
    val sortOrder: Int
)
