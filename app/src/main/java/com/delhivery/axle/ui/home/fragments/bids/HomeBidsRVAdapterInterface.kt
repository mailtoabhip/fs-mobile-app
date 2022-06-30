package com.delhivery.axle.ui.home.fragments.bids

import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request

interface HomeBidsRVAdapterInterface : ItemClickListener<BaseHomeBidsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseHomeBidsRVAdapterItem<*>) {
    if (item.type == Request) {
      handleAction(HomeBidsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeBidsRVAdapterItem<*>
  )

  fun getTotalOffers(origin_id:String?, dest_id:String?, tid:String?):Triple<Boolean?, Pair<String?, String?>?, Pair<String?, String?>?>?

  fun callShareRate(data: HomeBidsRequestItemData?, itemTD:String?, offerTD:String?, occ:String?, dcc:String?)


}