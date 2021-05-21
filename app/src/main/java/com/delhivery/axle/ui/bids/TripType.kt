package com.delhivery.axle.ui.bids

import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.In_Transit
import com.delhivery.axle.data.home.trips.TripStatus.TruckArrived
import com.delhivery.axle.data.home.trips.TripStatus.TruckConfirmed
import com.delhivery.axle.data.home.trips.TripStatus.TruckReached
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckLoaded
import com.delhivery.axle.data.home.trips.TripStatus.Recovery
import com.delhivery.axle.data.home.trips.TripStatus.TripCompleted

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Enum to hold various trip statuses
 *
 **
 */
enum class TripType(
  val typeId: Int,
  val status: List<String>,
  val typeText: String,
  private val title: String
) {
  Unknown(-1, listOf(TripStatus.Unknown.statusKey), "NA", "NA"),
  AwaitingArrival(
      0, listOf(TruckConfirmed.statusKey),
      "Awaiting Arrival", "Awaiting Arrival Trips"
  ),
  InTransit(
      1, listOf(In_Transit.statusKey),
      "InTransit", "InTransit Trips"
  ),
  AwaitingLoading(
      2, listOf(TruckArrived.statusKey),
      "Awaiting Loading", "Awaiting Loading Trips"
  ),
  AwaitingUnloading(
      3, listOf(TruckReached.statusKey),
      "Awaiting Unloading", "Awaiting Unloading Trips"
  );

  /**
   * Get toolbar title with count of items
   */
  fun toolbarTitle(count: Int = 0) = when (count) {
    0 -> title
    else -> "$title($count)"
  }

  companion object {
    /**
     * Get [TripType] by type id
     */
    fun byTypeId(typeId: Int) = values().firstOrNull { it.typeId == typeId } ?: Unknown

  }
}

enum class ViewPaymentType(
  val typeId: Int,
  val status: List<String>,
  val typeText: String,
  private val title: String
) {
  NA(-1, listOf(TripStatus.Unknown.statusKey), "NA", "NA"),
  AdvancePending(
      0,
      listOf(TruckArrived.statusKey, TruckConfirmed.statusKey),
      "Advance Pending", "Advance Pending Trips"
  ),
  BalancePending(
      1, listOf(TripCompleted.statusKey),
      "Balance Pending", "Balance Pending Trips "
  ),
  RecoveryPending(
      2, listOf(TripCompleted.statusKey),
      "Recovery Pending", "Recovery Pending Trips "
  );

  /**
   * Get toolbar title with count of items
   */
  fun toolbarTitle(count: Int = 0) = when (count) {
    0 -> title
    else -> "$title($count)"
  }

  companion object {
    /**
     * Get [ViewPaymentType] by type id
     */
    fun byTypeId(typeId: Int) = values().firstOrNull { it.typeId == typeId } ?: NA

  }
}