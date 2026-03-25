package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FastagStatusResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null
)
