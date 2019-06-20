package com.delhivery.orion.ui.bids

import com.delhivery.orion.data.home.trips.TripStatus
import com.delhivery.orion.data.home.trips.TripStatus.In_Transit
import com.delhivery.orion.data.home.trips.TripStatus.TripCompleted
import com.delhivery.orion.data.home.trips.TripStatus.TruckArrived
import com.delhivery.orion.data.home.trips.TripStatus.TruckConfirmed
import com.delhivery.orion.data.home.trips.TripStatus.TruckLoaded
import com.delhivery.orion.data.home.trips.TripStatus.TruckReached
import com.delhivery.orion.data.home.trips.TripStatus.TruckUnloaded

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
  private val title: String
) {
  Unknown(-1, listOf(TripStatus.Unknown.statusKey), "na (%d"),
  AdvancePending(
      0,
      listOf(TruckArrived.statusKey, TruckConfirmed.statusKey),
      "Advance Pending trips(%d)"
  ),
  InTransit(
      1,
      listOf(TruckLoaded.statusKey, TruckReached.statusKey, In_Transit.statusKey),
      "InTransit trips (%s)"
  ),
  BalancePending(
      2,
      listOf(TruckUnloaded.statusKey),
      "Balance Pending trips (%d)"
  ),
  Completed(
      3,
      listOf(TripCompleted.statusKey),
      "Completed trips (%d)"
  );

  /**
   * Get toolbar title with count of items
   */
  fun toolbarTitle(count: Int = 0) = String.format(title, count)

  companion object {
    /**
     * Get [TripType] by type id
     */
    fun byTypeId(typeId: Int) = values().filter { it.typeId == typeId }.firstOrNull() ?: Unknown
  }
}