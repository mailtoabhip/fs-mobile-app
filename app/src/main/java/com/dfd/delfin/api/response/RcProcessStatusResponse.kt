package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class RcProcessStatusResponse(
    @SerializedName("status")
    val status: String?,

    @SerializedName("current_step")
    val currentStep: String?,

    @SerializedName("completed_steps")
    val completedSteps: List<String>?,

    @SerializedName("journey_id")
    val journeyId: String?,

    @SerializedName("skip_vehicle_image_upload")
    val skipVehicleImageUpload: Boolean? = null
)
