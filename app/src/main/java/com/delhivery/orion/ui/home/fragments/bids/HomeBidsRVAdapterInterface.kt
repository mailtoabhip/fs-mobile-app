package com.delhivery.orion.ui.home.fragments.bids

import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

interface HomeBidsRVAdapterInterface : ItemClickListener<BaseHomeBidsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseHomeBidsRVAdapterItem<*>) {
    //useless for now
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeBidsRVAdapterItem<*>
  )
}