package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FastagImageValidateResponse(
    @SerializedName("journey_id")
    val journeyId: String? = null
)
