package com.delhivery.orion.ui.home.fragments.trips

import com.delhivery.orion.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem

interface HomeTripsRVAdapterInterface : ItemClickListener<BaseHomeTripsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseHomeTripsRVAdapterItem<*>) {
    if (item.type == TripItem) {
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
}