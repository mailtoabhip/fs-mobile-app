package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class PaymentBreakupRequest(
    @SerializedName("sales_code")
    val salesCode: String,
    @SerializedName("payment_method")
    val paymentMethod: String,
    @SerializedName("items")
    val items: List<PaymentBreakupItem>
)

data class PaymentBreakupItem(
    @SerializedName("vehicle_class")
    val vehicleClass: String,
    @SerializedName("quantity")
    val quantity: Int
) : java.io.Serializable
