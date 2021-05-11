package com.delhivery.axle.ui.tripdetails

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemAction
import com.delhivery.axle.databinding.ViewProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewTripPaymentSummaryDetailItemBinding
import com.delhivery.axle.databinding.ViewTripPaymentSummaryItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.ledger.BaseConsolidatedPageRVAdapterViewHolder
import com.delhivery.axle.ui.ledger.ConsolidatedPageProgressItem
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterInterface
import com.delhivery.axle.ui.ledger.ConsolidatedPageTimeoutItem

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
    binding.root.clickToAction(TripPaymentSummaryItemAction, item, adapterPosition, _interface)
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
    binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
  }
}