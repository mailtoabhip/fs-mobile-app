package com.dfd.delfin.ui.profile.raterewards.fragments.sharerate

import com.dfd.delfin.data.sharerates.ShareRatesItemDataAction_ViewDetails
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.ShareRate

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