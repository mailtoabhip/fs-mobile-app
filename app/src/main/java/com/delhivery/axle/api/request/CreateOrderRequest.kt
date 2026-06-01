package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class CreateOrderRequest(
    @SerializedName("sales_code")
    val salesCode: String,
    @SerializedName("customer_name")
    val customerName: String,
    @SerializedName("customer_mobile")
    val customerMobile: String,
    @SerializedName("vehicles")
    val vehicles: List<OrderVehicleItem>,
    @SerializedName("total_amount")
    val totalAmount: String,
    @SerializedName("idempotency_key")
    val idempotencyKey: String
)

data class OrderVehicleItem(
    @SerializedName("vrn")
    val vrn: String,
    @SerializedName("vehicle_class")
    val vehicleClass: String,
    @SerializedName("unit_price")
    val unitPrice: String
)
