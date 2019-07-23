package com.delhivery.axle.ui.searchload.fragments.searchresults

import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request

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

  /**
   * Handle specific action with item position
   */
  fun handleAction(
    actionId: String,
    item: BaseSearchLoadsRVAdapterItem<*>,
    position: Int
  )
}