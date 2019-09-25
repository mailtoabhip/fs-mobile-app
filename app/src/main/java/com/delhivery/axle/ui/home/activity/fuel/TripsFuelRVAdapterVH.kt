package com.delhivery.axle.ui.home.activity.fuel

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewFuelTripsProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewTripFuelItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Base home trips rv adapter view holder
 */
abstract class BaseTripsFuelRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseTripsFuelRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: TripsFuelRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: TripsFuelRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: TripsFuelRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Fuel trips item view holder
 */
class TripsFuelItemVH(binding: ViewTripFuelItemBinding) :
    BaseTripsFuelRVAdapterViewHolder<ViewTripFuelItemBinding, TripsFuelDataItem>(
        binding
    ) {
  override fun bind(
    item: TripsFuelDataItem,
    _interface: TripsFuelRVAdapterInterface
  ) {
    binding.trip = item.data
  }
}

/**
 * Fuel trips Progress view holder
 */
internal class TripsFuelProgressItemVH(binding: ViewFuelTripsProgressItemBinding) :
    BaseTripsFuelRVAdapterViewHolder<ViewFuelTripsProgressItemBinding, TripsFuelProgressItem>(
        binding
    ) {
  override fun bind(
    item: TripsFuelProgressItem,
    _interface: TripsFuelRVAdapterInterface
  ) {
  }
}

/**
 * Fuel trips warning item view holder
 */
internal class TripsFuelWarningItemVH(binding: ViewWarningItemBinding) :
    BaseTripsFuelRVAdapterViewHolder<ViewWarningItemBinding, TransactionWarningItem>(binding) {
  override fun bind(
    item: TransactionWarningItem,
    _interface: TripsFuelRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Fuel trips timeout view holder
 */
internal class TripsFuelTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseTripsFuelRVAdapterViewHolder<ViewTimeOutItemBinding, TransactionTimeoutItem>(binding) {
  override fun bind(
    item: TransactionTimeoutItem,
    _interface: TripsFuelRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}