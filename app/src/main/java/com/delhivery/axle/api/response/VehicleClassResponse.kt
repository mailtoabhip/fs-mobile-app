package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class VehicleClassResponse(
    @SerializedName("count")
    val count: Int,
    @SerializedName("vehicle_classes")
    val vehicleClasses: List<VehicleClassData>
)

data class VehicleClassData(
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
    @SerializedName("sort_order")
    val sortOrder: Int
)
