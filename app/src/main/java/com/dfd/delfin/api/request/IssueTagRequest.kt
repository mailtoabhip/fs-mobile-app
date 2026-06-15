package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class IssueTagRequest(
    @SerializedName("journey_id")
    val journeyId: String,

    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("order_item_id")
    val orderItemId: Int,

    @SerializedName("barcode")
    val barcode: String,

    @SerializedName("otp")
    val otp: String
)
