package com.dfd.delfin.ui.searchtrip

import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

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