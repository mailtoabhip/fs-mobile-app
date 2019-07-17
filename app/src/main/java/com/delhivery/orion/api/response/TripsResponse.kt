package com.delhivery.orion.api.response

import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.google.gson.annotations.SerializedName

data class TripsResponse(
  @SerializedName("count") val total: Int,
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("results") val trips: List<HomeTripsItemData>
)

data class TripSummaryResponse(
  @SerializedName("advance_pending") val advancePending: Summary,
  @SerializedName("balance_pending") val balancePending: Summary,
  @SerializedName("completed") val completed: Summary,
  @SerializedName("in_transit") val inTransit: Summary
)

data class Summary(
  @SerializedName("amount") val amount: Double? = null,
  @SerializedName("count") val count: Int? = null
) {

  fun count() = when (count) {
    null -> ""
    1 -> "$count trip"
    else -> "$count trips"
  }

  fun amount() = when (amount) {
    null -> ""
    else -> "₹ ${String.format("%, .0f", amount)}"
  }
}