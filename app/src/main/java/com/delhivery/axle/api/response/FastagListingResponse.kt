package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FastagListingResponse(
    @SerializedName("total_fastag_vehicles")
    val totalFastagVehicles: Int?,

    @SerializedName("has_next")
    val hasNext: Boolean?,

    @SerializedName("next_offset")
    val nextOffset: Int?,

    @SerializedName("result")
    val result: List<FastagVehicle>?
)

data class FastagVehicle(
    @SerializedName("fastag_id")
    val fastagId: String?,

    @SerializedName("fastag_vrn")
    val fastagVrn: String?,

    @SerializedName("fastag_balance")
    val fastagBalance: String?,

    @SerializedName("fastag_issued_by")
    val fastagIssuedBy: String?,

    @SerializedName("fastag_tag_status")
    val fastagTagStatus: String?,

    @SerializedName("vehicle_make")
    val vehicleMake: String?
)
