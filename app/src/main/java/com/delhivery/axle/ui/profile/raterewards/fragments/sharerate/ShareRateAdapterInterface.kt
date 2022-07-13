package com.delhivery.axle.ui.profile.raterewards.fragments.sharerate

import com.delhivery.axle.data.sharerates.ShareRatesItemDataAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.ShareRate

/**
 * Adapter interface for [ShareRates fragment]
 */
interface ShareRateAdapterInterface : ItemClickListener<BaseShareRateRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseShareRateRVAdapterItem<*>) {
    if (item.type == ShareRate)
    handleAction(ShareRatesItemDataAction_ViewDetails,item)
  }

  /**
   * Handle specific action with item position
   */
  fun handleAction(
    actionId: String,
    item: BaseShareRateRVAdapterItem<*>
  )


}