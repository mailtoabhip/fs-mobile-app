package com.delhivery.axle.ui.bids

import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.In_Transit
import com.delhivery.axle.data.home.trips.TripStatus.TripCompleted
import com.delhivery.axle.data.home.trips.TripStatus.TruckArrived
import com.delhivery.axle.data.home.trips.TripStatus.TruckConfirmed
import com.delhivery.axle.data.home.trips.TripStatus.TruckLoaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckReached
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded

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
  AdvancePending(
      0,
      listOf(TruckArrived.statusKey, TruckConfirmed.statusKey, TruckLoaded.statusKey),
      "Advance Pending", "Advance Pending trips"
  ),
  InTransit(
      1,
      listOf(TruckReached.statusKey, In_Transit.statusKey),
      "InTransit", "InTransit trips"
  ),
  BalancePending(
      2,
      listOf(TruckUnloaded.statusKey, EPodUploaded.statusKey),
      "Balance Pending", "Balance Pending trips "
  ),
  Completed(
      3,
      listOf(TripCompleted.statusKey),
      "Completed", "Completed trips"
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

    fun byStatus(_status: String) = when (_status) {
      TruckArrived.statusKey, TruckConfirmed.statusKey, TruckLoaded.statusKey -> AdvancePending
      TruckReached.statusKey, In_Transit.statusKey -> InTransit
      TruckUnloaded.statusKey -> BalancePending
      TripCompleted.statusKey -> Completed
      else -> Unknown
    }

  }
}