package com.dfd.delfin.api.response

import com.dfd.delfin.data.home.trucks.HomeTrucksRequestItemData
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

