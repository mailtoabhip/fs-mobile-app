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

    @SerializedName("is_commercial")
    val isCommercial: Boolean? = null,

    @SerializedName("exempted_state")
    val exemptedState: String? = null,

    @SerializedName("tag_color")
    val tagColor: String? = null,

    @SerializedName("bank")
    val bank: String? = null,

    @SerializedName("status")
    val status: String? = null
)
