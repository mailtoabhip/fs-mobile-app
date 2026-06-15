package com.dfd.delfin.ui.searchload.fragments.searchresults

import com.dfd.delfin.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Contracts
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request

/**
 * Adapter interface for [SearchLoadsFragment]
 */
interface SearchLoadsRVAdapterInterface : ItemClickListener<BaseSearchLoadsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseSearchLoadsRVAdapterItem<*>) {
    if (item.type == Request || item.type==Contracts) {
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

  fun deleteItem(item: BaseSearchLoadsRVAdapterItem<*>, position: Int)
}