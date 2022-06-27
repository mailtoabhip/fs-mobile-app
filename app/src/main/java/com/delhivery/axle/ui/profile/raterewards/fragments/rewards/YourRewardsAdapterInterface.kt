package com.delhivery.axle.ui.profile.raterewards.fragments.rewards

import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.yourrewards.YourRewardsItemDataAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.BaseYourRewardsRVAdapterItem
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsRVAdapterItemType.YourRewards

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