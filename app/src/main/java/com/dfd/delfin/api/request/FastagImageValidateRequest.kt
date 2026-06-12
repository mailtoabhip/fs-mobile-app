package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class FastagImageValidateRequest(
    @SerializedName("journey_id")
    val journeyId: String,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("order_item_id")
    val orderItemId: String
)
