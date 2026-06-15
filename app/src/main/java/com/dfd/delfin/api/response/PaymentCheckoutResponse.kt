package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class PaymentCheckoutResponse(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("payment_status")
    val paymentStatus: String,
    @SerializedName("payment_txn_id")
    val paymentTxnId: String,
    @SerializedName("total_amount")
    val totalAmount: Double,
    @SerializedName("message")
    val message: String
)
