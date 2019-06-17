package com.delhivery.orion.ui.selectroute.fragments.routeslist

import android.databinding.ViewDataBinding
import android.view.View
import com.delhivery.orion.data.home.routes.RoutesAction_AddRoute
import com.delhivery.orion.data.home.routes.RoutesAction_ViewDetails
import com.delhivery.orion.databinding.ViewAddRouteItemBinding
import com.delhivery.orion.databinding.ViewRouteProgressItemBinding
import com.delhivery.orion.databinding.ViewSelectRouteItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder

/**
 * Base Home bids RV adapter view holder
 */
abstract class RoutesRVAdapterViewHolder<out B : ViewDataBinding, IT : RoutesRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _clickListener: RoutesRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: RoutesRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: RoutesRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Route item view holder
 */
class RoutesItemVH(
  binding: ViewSelectRouteItemBinding
) : RoutesRVAdapterViewHolder<ViewSelectRouteItemBinding, RoutesRequestItem>(
    binding
) {
  override fun bind(
    item: RoutesRequestItem,
    _clickListener: RoutesRVAdapterInterface
  ) {
    binding.route = item.data
    binding.root.clickToAction(RoutesAction_ViewDetails, item, _clickListener)
  }
}

/**
 * Route progress view holder
 */
class RoutesProgressItemVH(
  binding: ViewRouteProgressItemBinding
) : RoutesRVAdapterViewHolder<ViewRouteProgressItemBinding, RoutesProgressItem>(
    binding
) {

  override fun bind(
    item: RoutesProgressItem,
    _clickListener: RoutesRVAdapterInterface
  ) {
  }
}

/**
 * Add Route item view holder
 */
class AddRoutesItemVH(
  binding: ViewAddRouteItemBinding
) : RoutesRVAdapterViewHolder<ViewAddRouteItemBinding, RoutesAddItem>(
    binding
) {
  override fun bind(
    item: RoutesAddItem,
    _clickListener: RoutesRVAdapterInterface
  ) {
    binding.root.clickToAction(RoutesAction_AddRoute, item, _clickListener)
  }
}