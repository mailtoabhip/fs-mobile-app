package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class ConfirmCollectionRequest(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("sales_code")
    val salesCode: String,
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("products")
    val products: List<ConfirmCollectionProduct>
)

data class ConfirmCollectionProduct(
    @SerializedName("fastag_id")
    val fastagId: String,
    @SerializedName("barcode")
    val barcode: String,
    @SerializedName("vehicle_class")
    val vehicleClass: String,
    @SerializedName("vehicle_number")
    val vehicleNumber: String
)
