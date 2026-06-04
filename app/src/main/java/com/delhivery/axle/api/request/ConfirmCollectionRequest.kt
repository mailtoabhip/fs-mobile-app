package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class ConfirmCollectionRequest(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("sales_code")
    val salesCode: String
)
