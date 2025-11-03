package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Request model for initiating marketplace call
 */
data class InitiateCallRequest(
    @SerializedName("source")
    val source: String = "axle_marketplace",
    
    @SerializedName("transaction_id")
    val transactionId: String,
    
    @SerializedName("bid_id")
    val bidId: String
)

