package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for marketplace call initiation
 */
data class InitiateCallResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: List<BridgeNumberData>?
)

/**
 * Bridge number data for marketplace call
 */
data class BridgeNumberData(
    @SerializedName("bridge_number")
    val bridgeNumber: String?,
    
    @SerializedName("vendor")
    val vendor: String?,
    
    @SerializedName("expiry")
    val expiry: Long?
)

