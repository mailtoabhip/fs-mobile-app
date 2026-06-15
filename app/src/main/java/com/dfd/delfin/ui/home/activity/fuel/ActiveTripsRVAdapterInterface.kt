package com.dfd.delfin.ui.home.activity.fuel

import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Trip

/**
 * Adapter interface for [ActiveTripsActivity]
 */
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