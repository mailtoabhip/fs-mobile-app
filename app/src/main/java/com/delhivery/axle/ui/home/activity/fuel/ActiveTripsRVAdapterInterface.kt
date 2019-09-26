package com.delhivery.axle.ui.home.activity.fuel

import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Trip

interface ActiveTripsRVAdapterInterface : ItemClickListener<BaseActiveTripsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseActiveTripsRVAdapterItem<*>) {
    if (item.type == Trip) {
      handleAction(HomeTripsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseActiveTripsRVAdapterItem<*>
  )

}