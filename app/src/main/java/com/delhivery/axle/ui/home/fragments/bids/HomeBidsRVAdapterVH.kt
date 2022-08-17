package com.delhivery.axle.ui.home.fragments.bids

import android.util.Log
import androidx.databinding.ViewDataBinding
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.*
import com.delhivery.axle.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.utils.extensions.underline

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseHomeBidsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeBidsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Header item view holder
 */
internal class HomeBidsHeaderItemVH(binding: ViewHomeBidsHeaderItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsHeaderItemBinding, HomeBidsHeaderItem>(binding) {
  override fun bind(
    item: HomeBidsHeaderItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.myBids = when (item.data.myBids) {
      -1 -> ""
      else -> item.data.myBids.toString() + " Bids"
    }
    binding.confirmedBids = when (item.data.confirmedBid) {
      -1 -> ""
      else -> item.data.confirmedBid.toString() + " Bids"
    }
    binding.lostBids = when (item.data.lostBids) {
      -1 -> ""
      else -> item.data.lostBids.toString() + " Bids"
    }

    binding.viewMyBids.clickToAction(HomeBidsHeaderAction_MyBids, item, _interface)
    binding.viewConfirmedBids.clickToAction(HomeBidsHeaderAction_ConfirmedBids, item, _interface)
    binding.viewLostBids.clickToAction(HomeBidsHeaderAction_LostBids, item, _interface)
  }
}

/**
 * Search item view holder
 */
internal class HomeBidsSearchItemVH(binding: ViewHomeSearchItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeSearchItemBinding, HomeBidsSearchItem>(binding) {
  override fun bind(
    item: HomeBidsSearchItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.editQuery.hint = "Origin / Destination"
    binding.editQuery.clickToAction(HomeBidsSearchAction_Search, item, _interface)
  }
}

/**
 * Bid request item view holder
 */
class HomeBidsRequestItemVH(binding: ViewHomeBidsRequestItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsRequestItemBinding, HomeBidsRequestItem>(binding) {
  override fun bind(
    item: HomeBidsRequestItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.request = item.data
    Log.i("bulkTransactionBids",item.data.bulkTransactionBids?.size.toString())
    binding.moreBidsRecieved = item.data.bulkTransactionBids?.size -1
   // binding.moreBidsRecieved = 2
    binding.textMoreBids.underline = true
    binding.textMoreBids.clickToAction(HomeBidsRequestAction_ViewOtherDetails, item, _interface)

    if(item.data.bidStatus().statusKey.toLowerCase().equals("open")){
      binding.textBidStatus.setTextColor(context.resources.getColor(R.color.status_active))
      binding.textBidStatus.text = context.resources.getString(R.string.label_active)
    }else if(item.data.bidStatus().statusKey.toLowerCase().equals("accepted")){
      if(item.data.transactionBid?.clientConfirmationPending == false){
        binding.textBidStatus.setTextColor(context.resources.getColor(R.color.pending))
        binding.textBidStatus.text = context.resources.getString(R.string.label_pending)
      }else{
        binding.textBidStatus.setTextColor(context.resources.getColor(R.color.status_confirmed))
        binding.textBidStatus.text = context.resources.getString(R.string.label_confirm)
      }
    }else if(item.data.bidStatus().statusKey.toLowerCase().equals("rejected")) {
      binding.textBidStatus.text = context.resources.getString(R.string.label_lost)
      binding.textBidStatus.setTextColor(context.resources.getColor(R.color.status_lost))
    }else if(item.data.bidStatus().statusKey.toLowerCase().equals("cancelled")) {
      binding.textBidStatus.text = context.resources.getString(R.string.label_cancel)
      binding.textBidStatus.setTextColor(context.resources.getColor(R.color.status_lost))
    }

    val res = item.data.resOffer
    if(item.data.resOffer?.first?.first==null){
      _interface.getTotalOffers(item.data)
    }

   // val res = _interface.getTotalOffers(item.data.origin, item.data.destination, item.data.truckSpecification?.truckDispName)
     if(res!=null && res.first?.first == true){
      if(item.data.bidStatus().statusKey.equals("rejected")){
        binding.shareRateLay.visibility = View.VISIBLE
      }else{
        binding.shareRateLay.visibility = View.GONE
      }
    }else{
      binding.shareRateLay.visibility = View.GONE
    }

    binding.btnShareRate.setOnClickListener { _interface.callShareRate(item.data, res?.second?.first, res?.second?.second, res?.third?.first, res?.third?.second, res?.first?.second) }

  }
}

/**
 * Bids warning item view holder
 */
internal class HomeBidsWarningItemVH(binding: ViewWarningItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewWarningItemBinding, HomeBidsWarningItem>(binding) {
  override fun bind(
    item: HomeBidsWarningItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Bids timeout view holder
 */
internal class HomeBidsTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewTimeOutItemBinding, HomeBidsTimeoutItem>(binding) {
  override fun bind(
    item: HomeBidsTimeoutItem,
    _interface: HomeBidsRVAdapterInterface
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
internal class HomeBidsProgressItemVH(binding: ViewHomeBidsProgressItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsProgressItemBinding, HomeBidsProgressItem>(
        binding
    ) {
  override fun bind(
    item: HomeBidsProgressItem,
    _interface: HomeBidsRVAdapterInterface
  ) {

  }
}