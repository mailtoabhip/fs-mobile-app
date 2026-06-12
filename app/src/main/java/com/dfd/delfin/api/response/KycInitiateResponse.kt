package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class KycInitiateResponse(
    @SerializedName("journey_id")
    val journeyId: String,
    @SerializedName("next_stage")
    val nextStage: String,
    @SerializedName("mobileNumber")
    val mobileNumber: String?,
    @SerializedName("data")
    val stageData: KycInitiateStageData?
)

data class KycInitiateStageData(
    @SerializedName("mobileNumber")
    val mobileNumber: String?
)
