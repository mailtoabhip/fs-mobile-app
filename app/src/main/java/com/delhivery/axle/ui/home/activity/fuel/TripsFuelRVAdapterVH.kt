package com.delhivery.axle.ui.home.activity.fuel

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewHomeTripsProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewTransactionItemBinding
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
 * Transaction item view holder
 */
class TransactionsItemVH(binding: ViewTransactionItemBinding) :
    BaseTripsFuelRVAdapterViewHolder<ViewTransactionItemBinding, TransactionDataItem>(
        binding
    ) {
  override fun bind(
    item: TransactionDataItem,
    _interface: TripsFuelRVAdapterInterface
  ) {
    binding.transaction = item.data
  }
}

/**
 * Transaction Progress view holder
 */
internal class TransactionsProgressItemVH(binding: ViewHomeTripsProgressItemBinding) :
    BaseTripsFuelRVAdapterViewHolder<ViewHomeTripsProgressItemBinding, TransactionsProgressItem>(
        binding
    ) {
  override fun bind(
    item: TransactionsProgressItem,
    _interface: TripsFuelRVAdapterInterface
  ) {
  }
}

/**
 * Transaction warning item view holder
 */
internal class TransactionWarningItemVH(binding: ViewWarningItemBinding) :
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
 * Transaction timeout view holder
 */
internal class TransactionTimeOutItemVH(binding: ViewTimeOutItemBinding) :
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