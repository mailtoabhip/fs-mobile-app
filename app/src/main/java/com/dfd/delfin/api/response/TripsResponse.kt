package com.dfd.delfin.api.response

import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.utils.StringUtils
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
  @SerializedName("recovery_pending") val recoveryPending: Summary,
  @SerializedName("awaiting_arrival") val awaitingArrival: Summary,
  @SerializedName("in_transit") val inTransit: Summary,
  @SerializedName("awaiting_pod") val awaitingPod: Summary,
  @SerializedName("awaiting_loading") val awaitingLoading: Summary,
  @SerializedName("awaiting_unloading") val awaitingUnloading: Summary,
  @SerializedName("total") val totalTrips: Int?= 0,
  @SerializedName("trips_with_issue") val issueTrips: Int?= 0
)

/**
 * Child Response container for [TripSummaryResponse]
 */
data class Summary(
  @SerializedName("amount") val amount: Double? = 0.0,
  @SerializedName("count") val count: Int? = 0
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


data class TripPaymentResponse(
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("status") val status: String,
  @SerializedName("amount") val paymentAmount: Double ?=0.0,
  @SerializedName("fuel_payout") var fuelPayout: Double ?=0.0,
  @SerializedName("fuel_mobile_no") var fuelNumber : String? =""
)