package com.delhivery.orion.ui.bids

import com.delhivery.orion.data.home.trips.TripStatus
import com.delhivery.orion.data.home.trips.TripStatus.InTrasit
import com.delhivery.orion.data.home.trips.TripStatus.TripCompleted
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
  val status: TripStatus,
  private val title: String
) {
  Unknown(-1, TripStatus.Unknown, "na (%d"),
  AdvancePending(0, TruckUnloaded, "Advance Pending trips(%d)"),
  InTransit(1, InTrasit, "InTransit trips (%s)"),
  BalancePending(2, TruckUnloaded, "Balance Pending trips (%d)"),
  Completed(3, TripCompleted, "Completed trips (%d)");

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