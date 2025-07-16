package com.delhivery.axle.ui.home.fragments.bids

import android.util.Log
import androidx.databinding.ViewDataBinding
import android.view.View
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.data.home.bids.*
import com.delhivery.axle.databinding.CardCommonBidsBinding
import com.delhivery.axle.databinding.LoadDelhiveryIntercityBinding
import com.delhivery.axle.databinding.ViewBidsHeaderNewItemBinding
import com.delhivery.axle.databinding.ViewBidsSearchbarNewItemBinding
import com.delhivery.axle.databinding.ViewContractsBidItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.bids.BidType
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

  protected fun View.clickToActionWithStateSelection(
    actionId: String,
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  ) {
    setOnClickListener { action(actionId, item, _interface) }
    this.isSelected = true
  }

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
internal class HomeBidsHeaderItemVH(binding: ViewBidsHeaderNewItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewBidsHeaderNewItemBinding, HomeBidsHeaderItem>(binding) {
  override fun bind(
    item: HomeBidsHeaderItem,
    _interface: HomeBidsRVAdapterInterface
  ) {

      binding.ongoingCount = item.data.myBids
      binding.tvOngoing.clickToAction(HomeBidsHeaderAction_TabChangeActive, item, _interface)

      //
      binding.wonCount = item.data.confirmedBid
      binding.tvWon.clickToAction(HomeBidsHeaderAction_TabChangeConfirmed, item, _interface)

      //
      binding.lostCount = item.data.lostBids
      binding.tvLost.clickToAction(HomeBidsHeaderAction_TabChangeLost, item, _interface)

    when(item.data.bidType){
      BidType.ActiveBid -> {
        binding.tvOngoing.isSelected = true
        binding.tvWon.isSelected = false
        binding.tvLost.isSelected = false
      }

      BidType.ConfirmedBid -> {
        binding.tvOngoing.isSelected = false
        binding.tvWon.isSelected = true
        binding.tvLost.isSelected = false
      }

      BidType.LostBid -> {
        binding.tvOngoing.isSelected = false
        binding.tvWon.isSelected = false
        binding.tvLost.isSelected = true
      }

      else -> {
        //this space is intentionally left blank
      }

    }


//    binding.myBids = when (item.data.myBids) {
//      -1 -> ""
//      else -> item.data.myBids.toString() + " Bids"
//    }
//    binding.confirmedBids = when (item.data.confirmedBid) {
//      -1 -> ""
//      else -> item.data.confirmedBid.toString() + " Bids"
//    }
//    binding.lostBids = when (item.data.lostBids) {
//      -1 -> ""
//      else -> item.data.lostBids.toString() + " Bids"
//    }
//    binding.contractBids = when (item.data.contractBids) {
//      -1 -> ""
//      else -> item.data.contractBids.toString() + " Bids"
//    }
//
//    binding.viewMyBids.clickToAction(HomeBidsHeaderAction_MyBids, item, _interface)
//    binding.viewConfirmedBids.clickToAction(HomeBidsHeaderAction_ConfirmedBids, item, _interface)
//    binding.viewLostBids.clickToAction(HomeBidsHeaderAction_LostBids, item, _interface)
//    binding.viewContractBids.clickToAction(HomeBidsHeaderAction_ContractBids, item, _interface)
  }

}

/**
 * Search item view holder
 */
internal class HomeBidsSearchItemVH(binding: ViewBidsSearchbarNewItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewBidsSearchbarNewItemBinding, HomeBidsSearchItem>(binding) {
  override fun bind(
    item: HomeBidsSearchItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    //binding.editQuery.hint = "Origin / Destination"
    //binding.editQuery.clickToAction(HomeBidsSearchAction_Search, item, _interface)
  }
}

/**
 * Bid request item view holder
 */
class HomeBidsRequestItemVH(binding: CardCommonBidsBinding) :
    BaseHomeBidsRVAdapterViewHolder<CardCommonBidsBinding, HomeBidsRequestItem>(binding) {
  override fun bind(
    item: HomeBidsRequestItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.request = item.data
    //

  }
}

/**
 * Contracts Bid request item view holder
 */
class HomeContractsBidsRequestItemVH(binding: ViewContractsBidItemBinding) :
  BaseHomeBidsRVAdapterViewHolder<ViewContractsBidItemBinding, HomeContractsBidsRequestItem>(binding) {
  override fun bind(
    item: HomeContractsBidsRequestItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.request = item.data
    if(item.data.bidStatus().statusKey.lowercase().equals("open")){
      binding.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))
      binding.tvBidStatus.text = context.resources.getString(R.string.label_under_review)
      binding.tvBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_under_review_icon, 0, 0, 0)
      binding.tvBidStatus.background = ContextCompat.getDrawable(context,R.drawable.bg_all_rounded_under_review)
      binding.tvBidStatus.compoundDrawablePadding =8
    }else if(item.data.bidStatus().statusKey.lowercase().equals("accepted")){
        binding.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.bid_placed_green))
        binding.tvBidStatus.text = context.resources.getString(R.string.label_contract_won)
      binding.tvBidStatus.background = ContextCompat.getDrawable(context,R.drawable.bg_all_rounded_won)
      binding.tvBidStatus.compoundDrawablePadding =8
      binding.tvBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_up_green, 0, 0, 0)
    }else if(item.data.bidStatus().statusKey.lowercase().equals("rejected")) {
      binding.tvBidStatus.text = context.resources.getString(R.string.label_contract_lost)
      binding.tvBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_down, 0, 0, 0)
      binding.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.destructive_red))
      binding.tvBidStatus.background = ContextCompat.getDrawable(context,R.drawable.bg_all_rounded_lost_red)
      binding.tvBidStatus.compoundDrawablePadding =8
    }else if(item.data.bidStatus().statusKey.lowercase().equals("cancelled")) {
      binding.tvBidStatus.text = context.resources.getString(R.string.cancelled)
      binding.tvBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cancel_bid, 0, 0, 0)
      binding.tvBidStatus.setTextColor(ContextCompat.getColor(context,R.color.heading_black))
      binding.tvBidStatus.background = ContextCompat.getDrawable(context,R.drawable.bg_all_round_corner_light_grey)
      binding.tvBidStatus.compoundDrawablePadding =8
    }

    val res = item.data.resOffer
    if(item.data.resOffer?.first?.first==null){
      _interface.getTotalOffers(item.data)
    }
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