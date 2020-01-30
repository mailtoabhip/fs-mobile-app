package com.delhivery.axle.ui.home.fragments.pod

import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

interface HomePodRVAdapterInterface : ItemClickListener<BaseHomePodRVAdapterItem<*>> {

  override fun onItemClicked(item: BaseHomePodRVAdapterItem<*>) {}

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    position: Int,
    item: BaseHomePodRVAdapterItem<*>
  )

}