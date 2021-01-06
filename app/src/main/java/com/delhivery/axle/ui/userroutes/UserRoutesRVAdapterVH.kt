package com.delhivery.axle.ui.userroutes

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.routes.RoutesAction_ViewDetails
import com.delhivery.axle.databinding.ViewUserRoutesItemBinding
import com.delhivery.axle.databinding.ViewUserRoutesProgressItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 5/1/21
 */

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseUserRoutesRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseUserRouteRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: UserRoutesRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: UserRoutesRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Add on click listener for action with position
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: UserRoutesRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: UserRoutesRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }

  /**
   * Post action to UI with position
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    position: Int,
    _interface: UserRoutesRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item, position) }
}

/**
 * admin user item view holder
 */
class UserRoutesItemVH(binding: ViewUserRoutesItemBinding) :
    BaseUserRoutesRVAdapterViewHolder<ViewUserRoutesItemBinding, UserRouteItem>(
        binding
    ) {
  override fun bind(
    item: UserRouteItem,
    _interface: UserRoutesRVAdapterInterface
  ) {
    binding.route = item.data
    binding.layRoutes.clickToAction(RoutesAction_ViewDetails, item, _interface)
  }
}

/**
 * Progress inline view holder
 */
internal class UserRoutesProgressItemVH(binding: ViewUserRoutesProgressItemBinding) :
    BaseUserRoutesRVAdapterViewHolder<ViewUserRoutesProgressItemBinding, UserRouteProgressItem>(
        binding
    ) {
  override fun bind(
    item: UserRouteProgressItem,
    _interface: UserRoutesRVAdapterInterface
  ) {
  }
}