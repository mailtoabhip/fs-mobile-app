package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class VehicleImageProcessResponse(
    @SerializedName("job_id")
    val jobId: String?,

    @SerializedName("status")
    val status: String?
)
