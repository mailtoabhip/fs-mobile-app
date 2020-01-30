package com.delhivery.axle.ui.searchtrip

import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

interface SearchRVAdapterInterface : ItemClickListener<BaseSearchRVAdapterItem<*>> {

  override fun onItemClicked(
    item: BaseSearchRVAdapterItem<*>
  ) {
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseSearchRVAdapterItem<*>
  )
}