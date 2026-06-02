package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class ConfirmCollectionResponse(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("success_count")
    val successCount: Int,
    @SerializedName("failure_count")
    val failureCount: Int,
    @SerializedName("failure_barcode_ids")
    val failureBarcodeIds: List<String>,
    @SerializedName("message")
    val message: String
)
