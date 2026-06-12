package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

/**
 * Request model for dispute form submission
 */
data class DisputeSubmissionRequest(
    @SerializedName("disputeTypeCode")
    val disputeTypeCode: String,
    
    @SerializedName("transactionId")
    val transactionId: String?,
    
    @SerializedName("fastagId")
    val fastagId: String,
    
    @SerializedName("fieldData")
    val fieldData: Map<String, Any>
)
