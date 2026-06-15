package com.dfd.delfin.ui.searchtrip

import android.view.View
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.api.request.SearchAction_ResetTrip
import com.dfd.delfin.api.request.SearchAction_SearchTrip
import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.dfd.delfin.databinding.ViewHomeBidsProgressItemBinding
import com.dfd.delfin.databinding.ViewPodItemBinding
import com.dfd.delfin.databinding.ViewSearchQueryItemBinding
import com.dfd.delfin.databinding.ViewSearchedQueryItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewWarningItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseSearchRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseSearchRVAdapterItem<*>>(
  binding: B
) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: SearchRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: SearchRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: SearchRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Search query item view holder
 */
class SearchQueryItemVH(binding: ViewSearchQueryItemBinding) :
    BaseSearchRVAdapterViewHolder<ViewSearchQueryItemBinding, SearchQueryItem>(binding) {
  override fun bind(
    item: SearchQueryItem,
    _interface: SearchRVAdapterInterface
  ) {
    binding.item = item.data
    binding.btnSearch.clickToAction(SearchAction_SearchTrip, item, _interface)
  }
}

/**
 * Search queried item view holder
 */
class SearchedQueryItemVH(binding: ViewSearchedQueryItemBinding) :
    BaseSearchRVAdapterViewHolder<ViewSearchedQueryItemBinding, SearchedQueryItem>(binding) {
  override fun bind(
    item: SearchedQueryItem,
    _interface: SearchRVAdapterInterface
  ) {
    binding.result = item.data.getResultString()
    binding.btnReset.clickToAction(SearchAction_ResetTrip, item, _interface)
  }
}

/**
 * Search data item view holder
 */
class SearchDataItemVH(binding: ViewPodItemBinding) :
    BaseSearchRVAdapterViewHolder<ViewPodItemBinding, SearchDataItem>(binding) {
  override fun bind(
    item: SearchDataItem,
    _interface: SearchRVAdapterInterface
  ) {
    binding.trip = item.data
    binding.root.clickToAction(HomeTripsRequestAction_ViewDetails, item, _interface)
    binding.btnCourier.clickToAction(HomeTripsRequestAction_UploadTracking, item, _interface)
  }
}

/**
 * Search warning item view holder
 */
internal class SearchWarningItemVH(binding: ViewWarningItemBinding) :
    BaseSearchRVAdapterViewHolder<ViewWarningItemBinding, SearchWarningItem>(binding) {
  override fun bind(
    item: SearchWarningItem,
    _interface: SearchRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Search timeout view holder
 */
internal class SearchTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseSearchRVAdapterViewHolder<ViewTimeOutItemBinding, SearchTimeoutItem>(binding) {
  override fun bind(
    item: SearchTimeoutItem,
    _interface: SearchRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Progress inline viewholder
 */
internal class SearchProgressItemVH(binding: ViewHomeBidsProgressItemBinding) :
    BaseSearchRVAdapterViewHolder<ViewHomeBidsProgressItemBinding, SearchProgressItem>(
        binding
    ) {
  override fun bind(
    item: SearchProgressItem,
    _interface: SearchRVAdapterInterface
  ) {

  }
}