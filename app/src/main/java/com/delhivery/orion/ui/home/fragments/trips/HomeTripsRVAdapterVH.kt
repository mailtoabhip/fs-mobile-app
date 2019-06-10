package com.delhivery.orion.ui.home.fragments.trips

import android.databinding.ViewDataBinding
import android.view.View
import com.delhivery.orion.data.home.trips.HomeTripsHeaderAction_AdvancePending
import com.delhivery.orion.data.home.trips.HomeTripsHeaderAction_BalancePending
import com.delhivery.orion.data.home.trips.HomeTripsHeaderAction_Completed
import com.delhivery.orion.data.home.trips.HomeTripsHeaderAction_InTransit
import com.delhivery.orion.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.orion.databinding.ViewHomeSearchItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsDetailsItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsHeaderItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsProgressItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder

/**
 * Base home trips rv adapter view holder
 */
abstract class BaseHomeTripsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeTripsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: HomeTripsRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: HomeTripsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: HomeTripsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Trip Search item view holder
 */
internal class HomeTripsSearchItemVH(binding: ViewHomeSearchItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewHomeSearchItemBinding, HomeTripsSearchItem>(binding) {
  override fun bind(
    item: HomeTripsSearchItem,
    _interface: HomeTripsRVAdapterInterface
  ) {
    binding.editQuery.clickToAction(HomeTripsSearchAction_Search, item, _interface)
  }
}

/**
 * Trip item view holder
 */
class HomeTripsItemVH(binding: ViewHomeTripsDetailsItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewHomeTripsDetailsItemBinding, HomeTripsItem>(binding) {
  override fun bind(
    item: HomeTripsItem,
    _interface: HomeTripsRVAdapterInterface
  ) {
    binding.trip = item.data
  }
}

/**
 * Trip Progress viewholder
 */
internal class HomeTripsProgressItemVH(binding: ViewHomeTripsProgressItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewHomeTripsProgressItemBinding, HomeTripsProgressItem>(
        binding
    ) {
  override fun bind(
    item: HomeTripsProgressItem,
    _interface: HomeTripsRVAdapterInterface
  ) {
  }
}

/**
 * Trip header viewholder
 */
internal class HomeTripsHeaderItemVH(binding: ViewHomeTripsHeaderItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewHomeTripsHeaderItemBinding, HomeTripsHeaderItem>(
        binding
    ) {
  override fun bind(
    item: HomeTripsHeaderItem,
    _interface: HomeTripsRVAdapterInterface
  ) {
    binding.viewAdvancePending.clickToAction(HomeTripsHeaderAction_AdvancePending, item, _interface)
    binding.viewBalancePending.clickToAction(HomeTripsHeaderAction_BalancePending, item, _interface)
    binding.viewInTransit.clickToAction(HomeTripsHeaderAction_InTransit, item, _interface)
    binding.viewCompleted.clickToAction(HomeTripsHeaderAction_Completed, item, _interface)
  }
}