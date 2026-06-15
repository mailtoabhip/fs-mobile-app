package com.dfd.delfin.ui.searchongoingtrip

import android.view.View
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.dfd.delfin.databinding.ViewHomeBidsProgressItemBinding
import com.dfd.delfin.databinding.ViewHomeTripsRequestItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewWarningItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 13/5/21
 */

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseSearchOngoingTripRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseSearchOngoingTripRVAdapterItem<*>>(
  binding: B
) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: SearchOngoingTripRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: SearchOngoingTripRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: SearchOngoingTripRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Search data item view holder
 */
class SearchDataItemVH(binding: ViewHomeTripsRequestItemBinding) :
    BaseSearchOngoingTripRVAdapterViewHolder<ViewHomeTripsRequestItemBinding, SearchDataItem>(binding) {
  override fun bind(
    item: SearchDataItem,
    _interface: SearchOngoingTripRVAdapterInterface
  ) {
    binding.trip = item.data
    binding.root.clickToAction(HomeTripsRequestAction_ViewDetails, item, _interface)
  }
}

/**
 * Search warning item view holder
 */
internal class SearchWarningItemVH(binding: ViewWarningItemBinding) :
    BaseSearchOngoingTripRVAdapterViewHolder<ViewWarningItemBinding, SearchWarningItem>(binding) {
  override fun bind(
    item: SearchWarningItem,
    _interface: SearchOngoingTripRVAdapterInterface
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
    BaseSearchOngoingTripRVAdapterViewHolder<ViewTimeOutItemBinding, SearchTimeoutItem>(binding) {
  override fun bind(
    item: SearchTimeoutItem,
    _interface: SearchOngoingTripRVAdapterInterface
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
    BaseSearchOngoingTripRVAdapterViewHolder<ViewHomeBidsProgressItemBinding, SearchProgressItem>(
        binding
    ) {
  override fun bind(
    item: SearchProgressItem,
    _interface: SearchOngoingTripRVAdapterInterface
  ) {

  }
}