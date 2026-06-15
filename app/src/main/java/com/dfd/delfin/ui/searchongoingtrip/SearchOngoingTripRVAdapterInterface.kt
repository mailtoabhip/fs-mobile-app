package com.dfd.delfin.ui.searchongoingtrip

import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.searchongoingtrip.SearchOngoingTripRVAdapterItemType.TripItem

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 13/5/21
 */
interface SearchOngoingTripRVAdapterInterface : ItemClickListener<BaseSearchOngoingTripRVAdapterItem<*>> {

  override fun onItemClicked(
    item: BaseSearchOngoingTripRVAdapterItem<*>
  ) {
    if (item.type == TripItem) {
      handleAction(HomeTripsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseSearchOngoingTripRVAdapterItem<*>
  )
}