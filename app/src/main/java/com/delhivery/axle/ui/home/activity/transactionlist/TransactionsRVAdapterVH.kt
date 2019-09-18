package com.delhivery.axle.ui.home.activity.transactionlist

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewHomeTripsProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewTransactionHeaderItemBinding
import com.delhivery.axle.databinding.ViewTransactionItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Base home trips rv adapter view holder
 */
abstract class BaseTransactionsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseTransactionsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: TransactionsRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: TransactionsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: TransactionsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Transaction item view holder
 */
class TransactionsItemVH(binding: ViewTransactionItemBinding) :
    BaseTransactionsRVAdapterViewHolder<ViewTransactionItemBinding, TransactionDataItem>(
        binding
    ) {
  override fun bind(
    item: TransactionDataItem,
    _interface: TransactionsRVAdapterInterface
  ) {
  }
}

/**
 * Transaction Progress view holder
 */
internal class TransactionsProgressItemVH(binding: ViewHomeTripsProgressItemBinding) :
    BaseTransactionsRVAdapterViewHolder<ViewHomeTripsProgressItemBinding, TransactionsProgressItem>(
        binding
    ) {
  override fun bind(
    item: TransactionsProgressItem,
    _interface: TransactionsRVAdapterInterface
  ) {
  }
}

/**
 * Transaction header view holder
 */
internal class TransactionHeaderItemVH(binding: ViewTransactionHeaderItemBinding) :
    BaseTransactionsRVAdapterViewHolder<ViewTransactionHeaderItemBinding, TransactionHeaderItem>(
        binding
    ) {
  override fun bind(
    item: TransactionHeaderItem,
    _interface: TransactionsRVAdapterInterface
  ) {

  }
}

/**
 * Transaction warning item view holder
 */
internal class TransactionWarningItemVH(binding: ViewWarningItemBinding) :
    BaseTransactionsRVAdapterViewHolder<ViewWarningItemBinding, TransactionWarningItem>(binding) {
  override fun bind(
    item: TransactionWarningItem,
    _interface: TransactionsRVAdapterInterface
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
    BaseTransactionsRVAdapterViewHolder<ViewTimeOutItemBinding, TransactionTimeoutItem>(binding) {
  override fun bind(
    item: TransactionTimeoutItem,
    _interface: TransactionsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}