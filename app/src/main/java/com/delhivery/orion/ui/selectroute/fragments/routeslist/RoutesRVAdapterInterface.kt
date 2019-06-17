package com.delhivery.orion.ui.selectroute.fragments.routeslist

import com.delhivery.orion.data.home.routes.RoutesAction_ViewDetails
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.Request

interface RoutesRVAdapterInterface : ItemClickListener<RoutesRVAdapterItem<*>> {
  override fun onItemClicked(item: RoutesRVAdapterItem<*>) {
    if (item.type == Request) {
      handleAction(RoutesAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: RoutesRVAdapterItem<*>
  )
}