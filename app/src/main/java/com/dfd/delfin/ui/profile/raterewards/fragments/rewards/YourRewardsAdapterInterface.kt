package com.dfd.delfin.ui.profile.raterewards.fragments.rewards

import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

interface YourRewardsAdapterInterface: ItemClickListener<BaseYourRewardsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseYourRewardsRVAdapterItem<*>) {
  }

  /**
   * Handle specific action with item position
   */
  fun handleAction(
    actionId: String,
    item: BaseYourRewardsRVAdapterItem<*>,
    position:Int
  )
}