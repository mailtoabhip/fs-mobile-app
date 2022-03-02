package com.delhivery.axle.ui.kyc.gst

import android.util.Log
import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.gst.GstAction_ViewDetails
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import java.text.FieldPosition

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
    if (item.data.gstDetailItemData?.address == null) {
      _interface.fetchDetails(item.data)
    }

    binding.gstAddress.text = item.data.gstDetailItemData?.address
    binding.gstCallNum.text = item.data.gstDetailItemData?.phoneNumber
    binding.gstNum.text = item.data?.gstNumber
    binding.radioGst.isChecked = (_interface.fetchCurrSelected()!=null && _interface.fetchCurrSelected().equals(item.data.gstDetailItemData?.gstNumber))
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