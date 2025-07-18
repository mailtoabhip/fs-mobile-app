package com.delhivery.axle.ui.home.fragments.contracts

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.ContractType
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.contracts.HomeContractsFilterExpress
import com.delhivery.axle.data.home.contracts.HomeContractsFilterIntracity
import com.delhivery.axle.data.home.contracts.HomeContractsFilterNonExpress
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterAll
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterFixed
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterFlexible
import com.delhivery.axle.databinding.ViewHomeContractsFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsIntracityFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsProgressItemBinding
import com.delhivery.axle.databinding.ViewSearchContractsItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding

import com.delhivery.axle.ui.base.BaseViewHolder

import com.delhivery.axle.databinding.CardCommonTripsBidsBinding


abstract class BaseHomeContractsRVAdapterViewHolder<out B: ViewDataBinding, IT: BaseHomeContractsRVAdapterItem<*>>(binding: B):BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: HomeContractsRVAdapterInterface
  )

  var countDownTimer:CountDownTimer? = null

  /**
 * Add on click listener for action
 */
protected fun View.clickToAction(
  actionId: String,
  item: IT,
  _interface: HomeContractsRVAdapterInterface
) = setOnClickListener { action(actionId, item, _interface) }

/**
 * Add on click listener for action with position
 */
protected fun View.clickToAction(
  actionId: String,
  item: IT,
  position: Int,
  _interface: HomeContractsRVAdapterInterface
) = setOnClickListener { action(actionId, item, position, _interface) }

/**
 * Post action to UI
 */
protected fun View.action(
  actionId: String,
  item: IT,
  _interface: HomeContractsRVAdapterInterface
) = post { _interface.handleAction(actionId, item) }

/**
 * Post action to UI with position
 */
protected fun View.action(
  actionId: String,
  item: IT,
  position: Int,
  _interface: HomeContractsRVAdapterInterface
) = post { _interface.handleAction(actionId, item, position) }
}

/**
 * Bid request item view holder
 */
class HomeContractsRequestItemVH(binding: CardCommonTripsBidsBinding) :
  BaseHomeContractsRVAdapterViewHolder<CardCommonTripsBidsBinding, HomeContractsRequestItem>(
    binding
  ) {

  override fun bind(
    item: HomeContractsRequestItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
    binding.request = item.data
//    binding.includeBidTime6.containerError.request = item.data
    //hide contract and live bidding tags view
    binding.includeHeader1.includeHeader1.visibility = View.GONE
    //hide 7 days a week view
    binding.includeBidTime4.closingTime.visibility = View.GONE
    //hide strong/ weak bid view
    binding.strongBidCard.strongBidCard.visibility = View.GONE
    //
    binding.weakBidCard.weakBidCard.visibility = View.GONE
    //
    binding.includeBidTime6.bidAmount.visibility = View.GONE
    //
    binding.includeBidTime6.labelBidAmount.visibility = View.GONE
    //
    binding.includeBidTime6.clReviseBid.visibility = View.GONE
    //
    if(item.data.reportingTime!=null) {
      binding.includeBidTime6.containerError.clPlaceBidReportTime.visibility = View.VISIBLE
      binding.includeBidTime6.placeBidButton.visibility = View.GONE
    } else {
      binding.includeBidTime6.placeBidButton.visibility = View.VISIBLE
      binding.includeBidTime6.containerError.clPlaceBidReportTime.visibility = View.GONE

    }
    //

    //Log.d("DEBUG_LOG======>>>>>>>>>", ""+Gson().toJson(item))
    //set route details
    //binding.includeHeader2.tvLocationTitle = item.data.


    
    // Set up click listener for the place bid button
    Log.d("contractTag"," required On ${item.data._requiredOn} reqTime ${item.data.requiredAtWithTime()} reportTime ${item.data.reportingTime}")
    binding.includeBidTime6.placeBidButton.clickToAction(HomeBidsRequestAction_ViewDetails, item, _interface)
    binding.includeBidTime6.containerError.reportingTime.text = item.data.getTimeOfContracts()
    binding.includeBidTime6.containerError.placeBidButton.clickToAction(HomeBidsRequestAction_ViewDetails, item, _interface)
    
    // You can add more click listeners for other action buttons as needed
    // For example, if there are other buttons in the included layouts
  }

   fun stopCounter() {
    countDownTimer?.cancel()
    countDownTimer = null
  }

}

/**
 * Progress inline view holder
 */
internal class HomeContractsProgressItemVH(binding: ViewHomeContractsProgressItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewHomeContractsProgressItemBinding, HomeContractsProgressItem>(
    binding
  ) {
  override fun bind(
    item: HomeContractsProgressItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
  }
}

/**
 * Bids warning item view holder
 */
internal class HomeContractsWarningItemVH(binding: ViewWarningItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewWarningItemBinding, HomeContractsWarningItem>(
    binding
  ) {
  override fun bind(
    item: HomeContractsWarningItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}
/**
* Search item view holder
*/
internal class HomeContractsSearchItemVH(binding: ViewSearchContractsItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewSearchContractsItemBinding, HomeContractsSearchItem>(
    binding
  ) {
  override fun bind(
    item: HomeContractsSearchItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
    binding.editStickySearch.clickToAction(HomeContractsSearchAction_Search,item,_interface)
    binding.info.clickToAction(HomeContractsFilterInfo,item,_interface)

  }
}

/**
 * Loads filter view holder
 */
internal class HomeContractsFilterItemVH(binding: ViewHomeContractsFilterItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewHomeContractsFilterItemBinding, HomeContractsFilterItem>(binding) {
  override fun bind(
    item: HomeContractsFilterItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
     binding.nonExpressToggle.text =  "${context.getString(R.string.action_non_express)} (${item.data.nonExpressCount})"
     binding.expressToggle.text =   "${context.getString(string.action_express)} (${item.data.expressCount})"
     binding.intracityToggle.text =   "${context.getString(string.action_intracity)} (${item.data.intraCity})"
    // filter visibility based on user's demand type
        binding.expressToggle.visibility = if(item.data.userDemandType.contains(DemandType.Internal.type)&&item.data.userContractDemand)View.VISIBLE else View.GONE
        binding.nonExpressToggle.visibility = if((item.data.userDemandType.contains(DemandType.Internal.type)&&item.data.userContractDemand)||item.data.userDemandType.contains(DemandType.Others.type))View.VISIBLE else View.GONE
        binding.intracityToggle.visibility = if(item.data.userDemandType.contains(DemandType.Intracity.type)&&item.data.userContractDemand)View.VISIBLE else View.GONE

    when (item.data.filterType) {
     "Corporate"-> {
        binding.nonExpressToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        binding.expressToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.intracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.expressToggle.isSelected = false
        binding.nonExpressToggle.isSelected=true
        binding.intracityToggle.isSelected=false
      }
      "Internal"-> {
        binding.expressToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        binding.nonExpressToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.intracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.expressToggle.isSelected = true
        binding.nonExpressToggle.isSelected=false
        binding.intracityToggle.isSelected=false
      }
      "Intracity" -> {
        binding.nonExpressToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.expressToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.intracityToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        binding.expressToggle.isSelected = false
        binding.nonExpressToggle.isSelected=false
        binding.intracityToggle.isSelected=true
      }
    }

    binding.expressToggle.clickToAction(HomeContractsFilterExpress, item, _interface)
    binding.nonExpressToggle.clickToAction(HomeContractsFilterNonExpress, item, _interface)
    binding.intracityToggle.clickToAction(HomeContractsFilterIntracity, item, _interface)
   // binding.info.clickToAction(HomeContractsFilterInfo, item, _interface)
  }
}

/**
 * Loads intracity filter view holder
 */
internal class HomeContractsIntracityFilterItemVH(binding: ViewHomeContractsIntracityFilterItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewHomeContractsIntracityFilterItemBinding, HomeContractsIntracityFilterItem>(binding) {
  override fun bind(
    item: HomeContractsIntracityFilterItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
    when (item.data.filterType) {
      "All"-> {
        binding.allIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        binding.flexibleIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.flexibleIntracityToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_multiple_location,0,0,0)
        binding.fixedIntracityToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_place,0,0,0)
        binding.fixedIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.allIntracityToggle.isSelected=true
        binding.flexibleIntracityToggle.isSelected=false
        binding.fixedIntracityToggle.isSelected=false
      }
      "Flexible"-> {
        binding.allIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.flexibleIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        binding.flexibleIntracityToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_multiple_location_blue,0,0,0)
        binding.fixedIntracityToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_place,0,0,0)
        binding.fixedIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.allIntracityToggle.isSelected=false
        binding.flexibleIntracityToggle.isSelected=true
        binding.fixedIntracityToggle.isSelected=false
      }
     "Fixed"-> {
        binding.allIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.fixedIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        binding.fixedIntracityToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_place_blue,0,0,0)
        binding.flexibleIntracityToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_multiple_location,0,0,0)
        binding.flexibleIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.allIntracityToggle.isSelected=false
        binding.fixedIntracityToggle.isSelected = true
        binding.flexibleIntracityToggle.isSelected=false
      }
    }
    binding.fixedIntracityToggle.clickToAction(HomeContractsIntracityFilterFixed, item, _interface)
    binding.flexibleIntracityToggle.clickToAction(HomeContractsIntracityFilterFlexible, item, _interface)
    binding.allIntracityToggle.clickToAction(HomeContractsIntracityFilterAll, item, _interface)

  }
}
/**
 * Loads timeout view holder
 */
internal class HomeContractsTimeOutItemVH(binding: ViewTimeOutItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewTimeOutItemBinding, HomeContractsTimeoutItem>(binding) {
  override fun bind(
    item: HomeContractsTimeoutItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

