package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class KycVerifyResponse(
    @SerializedName("journey_id")
    val journeyId: String,
    @SerializedName("next_stage")
    val nextStage: String
)
