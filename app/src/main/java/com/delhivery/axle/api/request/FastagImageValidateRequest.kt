package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class FastagImageValidateRequest(
    @SerializedName("journey_id")
    val journeyId: String
)
