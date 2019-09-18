package com.delhivery.axle.ui.home.activity.transactionlist

import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem

interface TransactionsRVAdapterInterface : ItemClickListener<BaseTransactionsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseTransactionsRVAdapterItem<*>) {
    if (item.type == TripItem) {
      handleAction(HomeTripsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseTransactionsRVAdapterItem<*>
  )
}