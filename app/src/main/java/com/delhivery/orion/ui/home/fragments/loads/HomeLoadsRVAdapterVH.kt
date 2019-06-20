package com.delhivery.orion.ui.home.fragments.loads

import android.databinding.ViewDataBinding
import android.view.View
import com.delhivery.orion.databinding.ViewHomeLoadsInfoItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsSearchItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsWarningItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseHomeLoadsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeLoadsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: HomeLoadsRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: HomeLoadsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: HomeLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Bid request item view holder
 */
class HomeLoadsRequestItemVH(binding: ViewHomeLoadsRequestItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsRequestItemBinding, HomeLoadsRequestItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsRequestItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.request = item.data
  }
}

/**
 * Progress inline view holder
 */
internal class HomeLoadsProgressItemVH(binding: ViewHomeLoadsProgressItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsProgressItemBinding, HomeLoadsProgressItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsProgressItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
  }
}

/**
 * Search item view holder
 */
internal class HomeLoadsSearchItemVH(binding: ViewHomeLoadsSearchItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsSearchItemBinding, HomeLoadsSearchItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsSearchItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
  }
}

/**
 * Bids warning item view holder
 */
internal class HomeLoadsWarningItemVH(binding: ViewHomeLoadsWarningItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsWarningItemBinding, HomeLoadsWarningItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsWarningItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.data = item.data
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Info item view holder
 */
internal class HomeLoadsInfoItemVH(binding: ViewHomeLoadsInfoItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsInfoItemBinding, HomeLoadsInfoItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsInfoItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.data = item.data
  }
}