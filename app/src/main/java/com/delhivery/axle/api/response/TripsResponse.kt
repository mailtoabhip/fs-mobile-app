package com.delhivery.axle.api.response

import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName

/**
 * Response container for Trips api
 */
data class TripsResponse(
  @SerializedName("count") val total: Int,
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("results") val trips: List<HomeTripsItemData>
)

/**
 * Response container for Trips summary
 */
data class TripSummaryResponse(
  @SerializedName("advance_pending") val advancePending: Summary,
  @SerializedName("balance_pending") val balancePending: Summary,
  @SerializedName("completed") val completed: Summary,
  @SerializedName("in_transit") val inTransit: Summary
)

/**
 * Child Response container for [TripSummaryResponse]
 */
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
    else -> "₹ ${StringUtils.formatAmount(amount)}"
  }
}

/**
 * Response container for Upload POD api
 */
data class UploadPodResponse(
  @SerializedName("pod_url") val podUrl: String
)