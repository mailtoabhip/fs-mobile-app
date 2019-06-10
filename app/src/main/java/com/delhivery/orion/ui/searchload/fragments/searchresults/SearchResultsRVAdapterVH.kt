package com.delhivery.orion.ui.searchload.fragments.searchresults

import android.databinding.ViewDataBinding
import android.view.View
import com.delhivery.orion.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseSearchResultsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseSearchLoadsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: SearchLoadsRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: SearchLoadsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: SearchLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Bid request item view holder
 */
class SearchLoadsRequestItemVH(binding: ViewHomeLoadsRequestItemBinding) :
    BaseSearchResultsRVAdapterViewHolder<ViewHomeLoadsRequestItemBinding, SearchLoadsRequestItem>(
        binding
    ) {
  override fun bind(
    item: SearchLoadsRequestItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {
    binding.request = item.data
  }
}

/**
 * Search load dummy header
 */
internal class SearchLoadsSearchSpinnerItemVH(binding: ViewHomeBidsSearchSpinnerItemBinding) :
    BaseSearchResultsRVAdapterViewHolder<ViewHomeBidsSearchSpinnerItemBinding, SearchLoadsSearchSpinnerItem>(
        binding
    ) {
  override fun bind(
    item: SearchLoadsSearchSpinnerItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {

  }
}