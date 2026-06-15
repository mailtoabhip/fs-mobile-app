package com.dfd.delfin.ui.home.fragments.loads

import com.dfd.delfin.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request

interface HomeLoadsRVAdapterInterface : ItemClickListener<BaseHomeLoadsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseHomeLoadsRVAdapterItem<*>) {
    if (item.type == Request) {
      handleAction(HomeBidsRequestAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeLoadsRVAdapterItem<*>
  )

  /**
   * Handle specific action with item position
   */
  fun handleAction(
    actionId: String,
    item: BaseHomeLoadsRVAdapterItem<*>,
    position: Int
  )

  fun deleteItem( item: BaseHomeLoadsRVAdapterItem<*>, position: Int)

  fun fetchCurrSize():Int?

  fun itemDeleted():Boolean

  fun updateCurrSize(size:Int)

  fun itemDeleted(cp:Boolean)


}