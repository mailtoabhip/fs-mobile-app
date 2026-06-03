package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class BarcodeLookupResponse(
    @SerializedName("order_id")
    val orderId: String?,

    @SerializedName("order_item_id")
    val orderItemId: Int?,

    @SerializedName("barcode_id")
    val barcodeId: String?,

    @SerializedName("barcode")
    val barcode: String?,

    @SerializedName("vehicle_class")
    val vehicleClass: String?,

    @SerializedName("product_status")
    val productStatus: String?
)
