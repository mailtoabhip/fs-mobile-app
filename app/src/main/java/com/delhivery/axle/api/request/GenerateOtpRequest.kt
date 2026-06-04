package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class GenerateOtpRequest(
    @SerializedName("journey_id")
    val journeyId: String,

    @SerializedName("barcode")
    val barcode: String,

    @SerializedName("tag_id")
    val tagId: String
)
