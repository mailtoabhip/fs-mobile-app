package com.delhivery.axle.ui.home.fragments.contracts

import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Contracts

interface HomeContractsRVAdapterInterface:BaseDataRVAdapter.ItemClickListener<BaseHomeContractsRVAdapterItem<*>> {

  override fun onItemClicked(item: BaseHomeContractsRVAdapterItem<*>) {
    if (item.type == Contracts) {
      handleAction(HomeBidsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeContractsRVAdapterItem<*>
  )

  /**
   * Handle specific action with item position
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeContractsRVAdapterItem<*>,
    position: Int
  )
}