package com.delhivery.axle.ui.home.activity.fuel

import com.delhivery.axle.data.transactions.TransactionAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.home.activity.fuel.TripsFuelRVAdapterItemType.Trip

interface TripsFuelRVAdapterInterface : ItemClickListener<BaseTripsFuelRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseTripsFuelRVAdapterItem<*>) {
    if (item.type == Trip) {
      handleAction(TransactionAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseTripsFuelRVAdapterItem<*>
  )
}