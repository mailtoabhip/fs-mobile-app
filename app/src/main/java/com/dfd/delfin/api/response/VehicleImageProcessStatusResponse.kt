package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class VehicleImageProcessStatusResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("data")
    val data: VehicleImageProcessData?
)

data class VehicleImageProcessData(
    @SerializedName("journey_id")
    val journeyId: String?,
    @SerializedName("vehicle_class")
    val vehicleClass: String?,
    @SerializedName("skipped_idfc")
    val skippedIdfc: Boolean?
)
