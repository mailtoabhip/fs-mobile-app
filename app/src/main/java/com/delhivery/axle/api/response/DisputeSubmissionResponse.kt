package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for dispute submission
 */
data class DisputeSubmissionResponse(
    @SerializedName("Sr_id")
    val srId: String?,
    
    @SerializedName("dispute_status")
    val disputeStatus: String?
)
