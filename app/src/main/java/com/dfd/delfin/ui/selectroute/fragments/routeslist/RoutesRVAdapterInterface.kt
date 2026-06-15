package com.dfd.delfin.ui.selectroute.fragments.routeslist

import com.dfd.delfin.data.home.routes.RoutesAction_AddRoute
import com.dfd.delfin.data.home.routes.RoutesAction_ViewDetails
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.AddAction

interface RoutesRVAdapterInterface : ItemClickListener<RoutesRVAdapterItem<*>> {
  override fun onItemClicked(item: RoutesRVAdapterItem<*>) {
    when (item.type) {
      AddAction -> handleAction(RoutesAction_AddRoute, item)
      else -> handleAction(RoutesAction_ViewDetails, item)
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