package com.delhivery.orion.ui.searchload.fragments.searchresults

import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request

interface SearchLoadsRVAdapterInterface : ItemClickListener<BaseSearchLoadsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseSearchLoadsRVAdapterItem<*>) {
    if (item.type == Request) {
      handleAction(HomeBidsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseSearchLoadsRVAdapterItem<*>
  )
}