package com.delhivery.axle.ui.kyc.gst

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.api.request.SearchAction_ResetTrip
import com.delhivery.axle.api.request.SearchAction_SearchTrip
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadEpod
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Base Gst items RV adapter view holder
 */
abstract class BaseGstRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseGstRVAdapterItem<*>>(
  binding: B
) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: GstRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: GstRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: GstRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}


/**
 * Gst data item view holder
 */
class GstDataItemVH(binding: ViewGstRequestItemBinding) :
    BaseGstRVAdapterViewHolder<ViewGstRequestItemBinding, GstDataItem>(binding) {
  override fun bind(
    item: GstDataItem,
    _interface: GstRVAdapterInterface
  ) {
    binding.root.clickToAction(HomeTripsRequestAction_ViewDetails, item, _interface)
   }
}

/**
 * Search warning item view holder
 */
internal class GstWarningItemVH(binding: ViewWarningItemBinding) :
    BaseGstRVAdapterViewHolder<ViewWarningItemBinding, GstWarningItem>(binding) {
  override fun bind(
    item: GstWarningItem,
    _interface: GstRVAdapterInterface
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
internal class GstTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseGstRVAdapterViewHolder<ViewTimeOutItemBinding, GstTimeoutItem>(binding) {
  override fun bind(
    item: GstTimeoutItem,
    _interface: GstRVAdapterInterface
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
internal class GstProgressItemVH(binding: ViewGstProgressItemBinding) :
    BaseGstRVAdapterViewHolder<ViewGstProgressItemBinding, GstProgressItem>(
        binding
    ) {
  override fun bind(
    item: GstProgressItem,
    _interface: GstRVAdapterInterface
  ) {

  }
}