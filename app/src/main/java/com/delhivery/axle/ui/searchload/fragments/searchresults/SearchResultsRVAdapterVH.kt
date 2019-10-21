package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

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
   * Add on click listener for action with position
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: SearchLoadsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: SearchLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }

  /**
   * Post action to UI with position
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    position: Int,
    _interface: SearchLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item, position) }

}

/**
 * Search request item view holder
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
    binding.btnBid.clickToAction(HomeBidsRequestAction_PlaceBid, item, adapterPosition, _interface)
    binding.viewBidInfo.clickToAction(
        HomeBidsRequestAction_PlaceBid, item, adapterPosition, _interface
    )
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

/**
 * Search load warning item
 */
internal class SearchLoadsWarningItemVH(binding: ViewWarningItemBinding) :
    BaseSearchResultsRVAdapterViewHolder<ViewWarningItemBinding, SearchLoadsWarningItem>(
        binding
    ) {
  override fun bind(
    item: SearchLoadsWarningItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.btnAction.visibility = View.GONE
  }
}