package com.delhivery.axle.api.response

import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.google.gson.annotations.SerializedName

data class InventoryResponse(
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("count") val total: Int,
    @SerializedName("results") val trucks: List<HomeTrucksRequestItemData>
)
