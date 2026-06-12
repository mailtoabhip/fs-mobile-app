package com.dfd.delfin.ui.home.fragments.trips

import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsRVAdapterItemType.CompletedTrip
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem

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