package com.delhivery.axle.data.bids

import com.google.gson.annotations.SerializedName

data class BidSuggestion(
    @SerializedName("suggested_amount") val suggestedAmount: Int?,
    @SerializedName("message") val message: String,
)
