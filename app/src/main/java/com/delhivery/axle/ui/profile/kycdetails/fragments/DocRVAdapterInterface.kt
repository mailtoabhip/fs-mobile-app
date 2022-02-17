package com.delhivery.axle.ui.profile.kycdetails.fragments

import com.delhivery.axle.data.doc.DocDetailData
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

interface DocRVAdapterInterface : ItemClickListener<BaseDocRVAdapterItem<*>> {

  override fun onItemClicked(
          item: BaseDocRVAdapterItem<*>
  ) {
  }

  /**
   * Handle specific action
   */
  fun handleAction(
          actionId: String,
          item: BaseDocRVAdapterItem<*>
  )

  fun fetchDetails(data: DocDetailData)
}