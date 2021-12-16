package com.delhivery.axle.ui.home.fragments.trips

import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.home.fragments.loads.BaseHomeLoadsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.CompletedTrip
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem

/**
 * Adapter interface for [HomeTripsFragment]
 */
interface HomeTripsRVAdapterInterface : ItemClickListener<BaseHomeTripsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseHomeTripsRVAdapterItem<*>) {
    if (item.type == TripItem || item.type == CompletedTrip) {
      handleAction(HomeTripsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeTripsRVAdapterItem<*>
  )

  /**
   * Handle specific action with item position
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeTripsRVAdapterItem<*>,
    position: Int
  )
}