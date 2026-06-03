package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class OrderItemsResponse(
    @SerializedName("order_items")
    val orderItems: List<OrderItem>? = null
)

data class OrderItem(
    @SerializedName("order_item_id")
    val orderItemId: Int? = null,

    @SerializedName("order_id")
    val orderId: String? = null,

    @SerializedName("vehicle_class")
    val vehicleClass: String? = null,

    @SerializedName("vrn")
    val vrn: String? = null,

    @SerializedName("commercial_vehicle")
    val commercialVehicle: Boolean? = null,

    @SerializedName("exempted_status")
    val exemptedStatus: String? = null,

    @SerializedName("status")
    val status: String? = null
)
