package com.delhivery.axle.ui.searchongoingtrip

import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 13/5/21
 */
interface SearchOngoingTripRVAdapterInterface : ItemClickListener<BaseSearchOngoingTripRVAdapterItem<*>> {

  override fun onItemClicked(
    item: BaseSearchOngoingTripRVAdapterItem<*>
  ) {
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseSearchOngoingTripRVAdapterItem<*>
  )
}