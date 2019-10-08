package com.delhivery.axle.ui.home.activity.fuel

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ViewActiveTripItemBinding
import com.delhivery.axle.databinding.ViewActiveTripsProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Base home trips rv adapter view holder
 */
abstract class BaseActiveTripsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseActiveTripsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {

  /**
   * Binds item to adapter
   */
  abstract fun bind(
    item: IT,
    _interface: ActiveTripsRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: ActiveTripsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: ActiveTripsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Fuel trips item view holder
 */
class ActiveTripsItemVH(binding: ViewActiveTripItemBinding) :
    BaseActiveTripsRVAdapterViewHolder<ViewActiveTripItemBinding, ActiveTripFuelDataItem>(
        binding
    ) {
  override fun bind(
    item: ActiveTripFuelDataItem,
    _interface: ActiveTripsRVAdapterInterface
  ) {
    binding.trip = item.data
    binding.btnCreateCard.isEnabled = item.data.isFuelBalanceAvailable()
  }
}

/**
 * Fuel trips Progress view holder
 */
internal class ActiveTripsProgressItemVH(binding: ViewActiveTripsProgressItemBinding) :
    BaseActiveTripsRVAdapterViewHolder<ViewActiveTripsProgressItemBinding, ActiveTripProgressItem>(
        binding
    ) {
  override fun bind(
    item: ActiveTripProgressItem,
    _interface: ActiveTripsRVAdapterInterface
  ) {
  }
}

/**
 * Fuel trips warning item view holder
 */
internal class ActiveTripsWarningItemVH(binding: ViewWarningItemBinding) :
    BaseActiveTripsRVAdapterViewHolder<ViewWarningItemBinding, ActiveTripWarningItem>(
        binding
    ) {
  override fun bind(
    item: ActiveTripWarningItem,
    _interface: ActiveTripsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.img.setImageResource(R.drawable.ic_no_trips)
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Fuel trips timeout view holder
 */
internal class ActivetripsTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseActiveTripsRVAdapterViewHolder<ViewTimeOutItemBinding, ActiveTripTimeoutItem>(
        binding
    ) {
  override fun bind(
    item: ActiveTripTimeoutItem,
    _interface: ActiveTripsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}