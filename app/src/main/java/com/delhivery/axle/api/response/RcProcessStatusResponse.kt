package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class RcProcessStatusResponse(
    @SerializedName("status")
    val status: String?,

    @SerializedName("current_step")
    val currentStep: String?,

    @SerializedName("completed_steps")
    val completedSteps: List<String>?,

    @SerializedName("data")
    val data: RcProcessData?
)

data class RcProcessData(
    @SerializedName("journey_id")
    val journeyId: String?,

    @SerializedName("order_item_id")
    val orderItemId: Int?,

    @SerializedName("vrn")
    val vrn: String?,

    @SerializedName("vehicle_class")
    val vehicleClass: String?,

    @SerializedName("fuel_type")
    val fuelType: String?
)
