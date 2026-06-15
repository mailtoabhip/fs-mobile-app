package com.dfd.delfin.ui.home.fragments.trips

import android.view.View
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.data.home.trips.*
import com.dfd.delfin.databinding.ViewCompletedTripItemBinding
import com.dfd.delfin.databinding.ViewHomeSearchItemBinding
import com.dfd.delfin.databinding.ViewHomeTripsHeaderItemBinding
import com.dfd.delfin.databinding.ViewHomeTripsProgressItemBinding
import com.dfd.delfin.databinding.ViewHomeTripsRequestItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewWarningItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder

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
   * Add on click listener for action with position
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: HomeTripsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: HomeTripsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }

  /**
   * Post action to UI with position
   */
  protected fun View.action(
      actionId: String,
      item: IT,
      position: Int,
      _interface: HomeTripsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item, position) }
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
    binding.editQuery.hint = "Vehicle / Destination / LR"
    binding.editQuery.clickToAction(HomeTripsSearchAction_Search, item, _interface)
  }
}

/**
 * Trip item view holder
 */
class HomeTripsItemVH(binding: ViewHomeTripsRequestItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewHomeTripsRequestItemBinding, HomeTripsItem>(binding) {
  override fun bind(
    item: HomeTripsItem,
    _interface: HomeTripsRVAdapterInterface
  ) {
    binding.trip = item.data
    binding.llChangePaymentMode.clickToAction(HomeAdvancePendingPaymentMode, item, bindingAdapterPosition, _interface )
  }
}

/**
 * Completed Trip item view holder
 */
internal class HomeCompletedTripItemVH(binding: ViewCompletedTripItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewCompletedTripItemBinding, HomeCompletedTripItem>(binding) {
  override fun bind(
    item: HomeCompletedTripItem,
    _interface: HomeTripsRVAdapterInterface
  ) {
    binding.trip = item.data
  }
}

/**
 * Trip Progress view holder
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
 * Trip header view holder
 */
internal class HomeTripsHeaderItemVH(binding: ViewHomeTripsHeaderItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewHomeTripsHeaderItemBinding, HomeTripsHeaderItem>(
        binding
    ) {
  override fun bind(
    item: HomeTripsHeaderItem,
    _interface: HomeTripsRVAdapterInterface
  ) {
    binding.advancePending = when (item.data.advancePending) {
      null -> null
      else -> item.data.advancePending
    }

    binding.podPending = when (item.data.podPending) {
      null -> null
      else -> item.data.podPending
    }

    binding.inTransit = when (item.data.inTransit) {
      null -> null
      else -> item.data.inTransit
    }

    binding.completed = when (item.data.completed) {
      null -> null
      else -> item.data.completed
    }

    binding.viewAdvancePending.clickToAction(HomeTripsHeaderAction_AdvancePending, item, _interface)
    binding.viewPodPending.clickToAction(HomeTripsHeaderAction_PODPending, item, _interface)
    binding.viewInTransit.clickToAction(HomeTripsHeaderAction_InTransit, item, _interface)
    binding.viewCompleted.clickToAction(HomeTripsHeaderAction_Completed, item, _interface)
  }
}

/**
 * Bids warning item view holder
 */
internal class HomeTripsWarningItemVH(binding: ViewWarningItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewWarningItemBinding, HomeTripsWarningItem>(binding) {
  override fun bind(
    item: HomeTripsWarningItem,
    _interface: HomeTripsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Trips timeout view holder
 */
internal class HomeTripsTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewTimeOutItemBinding, HomeTripsTimeoutItem>(binding) {
  override fun bind(
    item: HomeTripsTimeoutItem,
    _interface: HomeTripsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}