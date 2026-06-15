package com.dfd.delfin.ui.kyc.gst

import com.dfd.delfin.data.gst.GstDetailData
import com.dfd.delfin.data.gst.GstDetailItemData
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

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

  fun fetchDetails(data: GstDetailData)

  fun fetchCurrSelected():String?

    fun fetchCheckedDetails(data: GstDetailItemData?)

}