package com.delhivery.axle.api.response

import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.google.gson.annotations.SerializedName

data class InventoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("count") val total: Int,
    @SerializedName("next_offset") val nextOffset: Int?,
    @SerializedName("results") val trucks: List<HomeTrucksRequestItemData>,
    @SerializedName("total_fastag_vehicles") val totalFastagVehicles: Int?
)

data class PricingResponse(
        @SerializedName("message") val message: String,
        @SerializedName("success") val success: Boolean
)

