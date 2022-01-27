package com.delhivery.axle.ui.kyc.gst

import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

interface GstRVAdapterInterface : ItemClickListener<BaseGstRVAdapterItem<*>> {

  override fun onItemClicked(
    item: BaseGstRVAdapterItem<*>
  ) {
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseGstRVAdapterItem<*>
  )
}