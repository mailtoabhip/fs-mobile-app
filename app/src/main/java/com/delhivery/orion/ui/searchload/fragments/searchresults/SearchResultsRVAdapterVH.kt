package com.delhivery.orion.ui.searchload.fragments.searchresults

import android.databinding.ViewDataBinding
import android.view.View
import com.delhivery.orion.data.home.bids.HomeBidsSearchAction_Search
import com.delhivery.orion.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseSearchResultsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseSearchResultsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: SearchResultsRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: SearchResultsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: SearchResultsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Search item view holder
 */
internal class SearchResultsSearchItemVH(binding: ViewHomeBidsSearchItemBinding) :
    BaseSearchResultsRVAdapterViewHolder<ViewHomeBidsSearchItemBinding, HomeBidsSearchItem>(
        binding
    ) {
  override fun bind(
    item: HomeBidsSearchItem,
    _interface: SearchResultsRVAdapterInterface
  ) {
    binding.loadRequests = item.data.loadRequests
    binding.editSearch.clickToAction(HomeBidsSearchAction_Search, item, _interface)
  }
}

/**
 * Bid request item view holder
 */
class SearchResultsRequestItemVH(binding: ViewHomeBidsRequestItemBinding) :
    BaseSearchResultsRVAdapterViewHolder<ViewHomeBidsRequestItemBinding, HomeBidsRequestItem>(
        binding
    ) {
  override fun bind(
    item: HomeBidsRequestItem,
    _interface: SearchResultsRVAdapterInterface
  ) {
    binding.request = item.data
  }
}

/**
 * Search load dummy header
 */
internal class SearchResultsSearchSpinnerItemVH(binding: ViewHomeBidsSearchSpinnerItemBinding) :
    BaseSearchResultsRVAdapterViewHolder<ViewHomeBidsSearchSpinnerItemBinding, HomeBidsSearchSpinnerItem>(
        binding
    ) {
  override fun bind(
    item: HomeBidsSearchSpinnerItem,
    _interface: SearchResultsRVAdapterInterface
  ) {

  }
}