package com.delhivery.orion.ui.home.fragments.loads

import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request

interface HomeLoadsRVAdapterInterface : ItemClickListener<BaseHomeLoadsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseHomeLoadsRVAdapterItem<*>) {
    if (item.type == Request) {
      handleAction(HomeBidsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeLoadsRVAdapterItem<*>
  )
}