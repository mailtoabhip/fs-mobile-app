package com.dfd.delfin.ui.home.fragments.pod

import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

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