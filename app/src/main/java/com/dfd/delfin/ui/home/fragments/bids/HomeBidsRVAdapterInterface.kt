package com.dfd.delfin.ui.home.fragments.bids

import com.dfd.delfin.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Contracts
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request

interface HomeBidsRVAdapterInterface : ItemClickListener<BaseHomeBidsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseHomeBidsRVAdapterItem<*>) {
    if (item.type == Request || item.type==Contracts) {
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

  fun getTotalOffers(data: HomeBidsRequestItemData?)

  fun callShareRate(data: HomeBidsRequestItemData?, itemTD:String?, offerTD:String?, occ:String?, dcc:String?, offerid:String?,amount:String?)


}