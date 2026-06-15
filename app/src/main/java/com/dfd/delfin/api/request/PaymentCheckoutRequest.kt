package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class PaymentCheckoutRequest(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("total_amount")
    val totalAmount: String,
    @SerializedName("idempotency_key")
    val idempotencyKey: String,
    @SerializedName("txn_remarks")
    val txnRemarks: String = "FASTag order payment"
)
