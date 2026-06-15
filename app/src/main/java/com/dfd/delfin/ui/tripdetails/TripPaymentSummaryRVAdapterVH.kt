package com.dfd.delfin.ui.tripdetails

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.data.tripdetail.TripPaymentSummaryDetailItemAction
import com.dfd.delfin.data.tripdetail.TripPaymentSummaryItemAction
import com.dfd.delfin.databinding.ViewProgressItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewTripPaymentSummaryDetailItemBinding
import com.dfd.delfin.databinding.ViewTripPaymentSummaryItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

abstract class BaseTripPaymentSummaryRVAdapterVH<out B: ViewDataBinding,
    IT : BaseTripPaymentSummaryRVAdapterItem<*>>(binding: B) : BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: TripPaymentSummaryRVAdapterInterface
  )

  /**
   * Add Click Listener for Action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: TripPaymentSummaryRVAdapterInterface
  ) = setOnClickListener{ action(actionId, item, position, _interface)}

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    position: Int,
    _interface: TripPaymentSummaryRVAdapterInterface
  ) = post {_interface.handleAction(actionId, position, item)}
}

/**
 * Trip payment summary item view holder
 */
class TripPaymentSummaryItemVH(binding: ViewTripPaymentSummaryItemBinding) :
    BaseTripPaymentSummaryRVAdapterVH<ViewTripPaymentSummaryItemBinding, TripSummaryItem>(binding) {
  override fun bind(
    item: TripSummaryItem,
    _interface: TripPaymentSummaryRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.amount = item.data.totalAmount()
    binding.item = item.data
    binding.root.clickToAction(TripPaymentSummaryItemAction, item, bindingAdapterPosition, _interface)
  }
}

/**
 * Trip payment summary detail item view holder
 */
class TripPaymentDetailSummaryItemVH(binding: ViewTripPaymentSummaryDetailItemBinding) :
    BaseTripPaymentSummaryRVAdapterVH<ViewTripPaymentSummaryDetailItemBinding, TripSummaryDetailItem>(binding) {
  override fun bind(
    item: TripSummaryDetailItem,
    _interface: TripPaymentSummaryRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subtitle = item.data.subTitle
    binding.amount = item.data.formattedAmount()
    binding.detailItem = item.data
    if (item.data.redirectable == true) {
      val text = SpannableString(item.data.title)
      text.setSpan(
          UnderlineSpan(),
          19, // start
          item.data.title.length, // end
          0 // flags
      )
      binding.textTitle.text = text
    }
    binding.textTitle.clickToAction(TripPaymentSummaryDetailItemAction, item, bindingAdapterPosition, _interface)
  }
}

/**
 * Trip payment summary progress item view holder
 * */
class TripPaymentSummaryProgressItemVH(binding: ViewProgressItemBinding) :
    BaseTripPaymentSummaryRVAdapterVH<ViewProgressItemBinding, TripSummaryProgressItem>(binding) {
  override fun bind(item: TripSummaryProgressItem, _interface: TripPaymentSummaryRVAdapterInterface
  ) {

  }
}

/**
 * Trip payment summary timeout item view holder
 * */
internal class TripPaymentSummaryTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseTripPaymentSummaryRVAdapterVH<ViewTimeOutItemBinding, TripSummaryTimeoutItem>(binding) {
  override fun bind(item: TripSummaryTimeoutItem, _interface: TripPaymentSummaryRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, bindingAdapterPosition, _interface)
  }
}