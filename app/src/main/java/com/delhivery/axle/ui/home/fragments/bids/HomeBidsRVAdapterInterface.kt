package com.delhivery.axle.ui.home.fragments.bids

import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request

interface HomeBidsRVAdapterInterface : ItemClickListener<BaseHomeBidsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseHomeBidsRVAdapterItem<*>) {
    if (item.type == Request) {
      handleAction(HomeBidsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeBidsRVAdapterItem<*>
  )
}