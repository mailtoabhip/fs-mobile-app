package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class FastagImageUploadResponse(
    @SerializedName("journey_id")
    val journeyId: String?
)
