package com.delhivery.axle.ui.tripdetails

import com.delhivery.axle.ui.base.adapter.BaseSummaryDataRVAdapter.ItemClickListener

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */
interface TripPaymentSummaryRVAdapterInterface: ItemClickListener<BaseTripPaymentSummaryRVAdapterItem<*>> {

  override fun onItemClicked(item: BaseTripPaymentSummaryRVAdapterItem<*>, position: Int) {}

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    position: Int,
    item: BaseTripPaymentSummaryRVAdapterItem<*>
  )

}