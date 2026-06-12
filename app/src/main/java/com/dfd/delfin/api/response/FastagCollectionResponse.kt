package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class FastagCollectionResponse(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("total_tags")
    val totalTags: Int,
    @SerializedName("inventory")
    val inventory: List<FastagInventoryItem>
)

data class FastagInventoryItem(
    @SerializedName("vehicle_class")
    val vehicleClass: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("vehicle_types")
    val vehicleTypes: List<String>,
    @SerializedName("units")
    val units: Int,
    @SerializedName("color_code")
    val colorCode: String
)
