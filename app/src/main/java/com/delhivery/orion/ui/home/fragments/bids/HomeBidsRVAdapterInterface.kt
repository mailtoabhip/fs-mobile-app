package com.delhivery.orion.ui.home.fragments.bids

import com.delhivery.orion.data.home.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request

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