package com.delhivery.axle.ui.userroutes

import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 5/1/21
 */
interface UserRoutesRVAdapterInterface : ItemClickListener<BaseUserRouteRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseUserRouteRVAdapterItem<*>) {

  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseUserRouteRVAdapterItem<*>
  )

  /**
   * Handle specific action with item position
   */
  fun handleAction(
    actionId: String,
    item: BaseUserRouteRVAdapterItem<*>,
    position: Int
  )
}