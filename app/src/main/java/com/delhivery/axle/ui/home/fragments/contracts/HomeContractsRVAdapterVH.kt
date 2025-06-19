package com.delhivery.axle.ui.home.fragments.contracts

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
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
import com.delhivery.axle.data.home.contracts.HomeContractsFilterExpress
import com.delhivery.axle.data.home.contracts.HomeContractsFilterIntracity
import com.delhivery.axle.data.home.contracts.HomeContractsFilterNonExpress
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterAll
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterFixed
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterFlexible
import com.delhivery.axle.data.home.loads.HomeLoadsSearchAction_Search
import com.delhivery.axle.databinding.ViewHomeContractsFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsIntracityFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsSearchItemBinding
import com.delhivery.axle.databinding.ViewSearchContractsItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.utils.StringUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
class HomeContractsRequestItemVH(binding: ViewHomeContractsRequestItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewHomeContractsRequestItemBinding, HomeContractsRequestItem>(
    binding
  ) {

  override fun bind(
    item: HomeContractsRequestItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
    binding.request = item.data
    if(item.data.isItIntraCityContract()){
      if(item.data.isFlexible){
        binding.tvIntracityHubOriginCity.text = if(item.data.secondaryReportingCenters!=null&&item.data.secondaryReportingCenters.size>1){
          HtmlCompat.fromHtml(context.getString(R.string.msg_more_reporting_centers,item.data.reportingCenters(),item.data.secondaryReportingCenters.size-1), HtmlCompat.FROM_HTML_MODE_LEGACY)}else item.data.reportingCenters()
        binding.intracityContractTypeChip.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_multiple_location,
          0,
          0,
          0
        )
      }else{
        binding.intracityContractTypeChip.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_place,
          0,
          0,
          0
        )
        binding.tvIntracityHubOriginCity.text =item.data.originCityName()
      }
    }else{
      binding.tvHubOriginCity.text = item.data.originCityName()
    }
    //Transaction Cancelled
    if(item.data.transactionStatus=="cancelled"){
      binding.userBidInfo.visibility= View.GONE
      binding.tvBidStatus.text = "Cancelled"
      binding.tvBidTime.visibility = View.GONE
      binding.tvBidStatus.setTextColor(
        ContextCompat.getColor(
          context,
          R.color.heading_black
        )
      )
      binding.statusImage.setImageDrawable(
        ContextCompat.getDrawable(
          context,
          R.drawable.ic_cancel_contract
        )
      )
      binding.clBidStatus.background = ContextCompat.getDrawable(
        context,
        R.drawable.bg_all_round_corner_light_grey
      )
    }else if (item.data.bidStatus() == Accepted || item.data.bidStatus() == Cancelled || item.data.bidStatus() == Rejected) {
      binding.userBidInfo.visibility= View.VISIBLE
      //Result status
      binding.tvBidStatus.setTextColor(
        ContextCompat.getColor(
          context,
          R.color.bid_placed_green
        )
      )
      binding.statusImage.setImageDrawable(
        ContextCompat.getDrawable(
          context,
          R.drawable.ic_bid_result
        )
      )
      binding.clBidStatus.background = ContextCompat.getDrawable(
        context,
        R.drawable.bg_all_rounded_won
      )
      binding.tvBidTime.visibility = View.GONE
      if (item.data.bidStatus() == Accepted) {
        //Bid Won
        binding.userBidStatus.text =
          "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
        binding.userBidInfo.text = " You Won"
        binding.userBidInfo.background = null
        binding.userBidInfo.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_thumbs_up_green,
          0,
          0,
          0
        )
      } else if (item.data.bidStatus() == Rejected) {
        //Bid Lost
        binding.userBidStatus.text =
          "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
        binding.userBidInfo.text = " You Lost"
        binding.userBidInfo.background = null
        binding.userBidInfo.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_thumbs_down,
          0,
          0,
          0
        )
      }
    }else if(item.data.transactionStatus=="allocated"){
      binding.userBidInfo.visibility= View.GONE
      //Result status
      binding.tvBidStatus.setTextColor(
        ContextCompat.getColor(
          context,
          R.color.bid_placed_green
        )
      )
      binding.statusImage.setImageDrawable(
        ContextCompat.getDrawable(
          context,
          R.drawable.ic_bid_result
        )
      )
      binding.clBidStatus.background = ContextCompat.getDrawable(
        context,
        R.drawable.bg_all_rounded_won
      )
      binding.tvBidTime.visibility = View.GONE
      binding.userBidStatus.text = "You did not bid"
    } else {
      binding.tvBidTime.visibility = View.VISIBLE
      //Bidding open
      if (item.data.isContractBiddingOpen()) {
        binding.userBidInfo.visibility= View.VISIBLE
        // Live bidding
        if (item.data.isUnderOneHour()) {
          binding.clBidStatus.visibility = View.VISIBLE
          binding.tvBidStatus.text = "Live Bidding"
          binding.tvBidStatus.setTextColor(
            ContextCompat.getColor(
              context,
              R.color.destructive_red
            )
          )
          GlideApp.with(context)
            .load(R.raw.livebidding)
            .apply(RequestOptions().override(40, 40))
            .into(binding.statusImage)

          binding.clBidStatus.background = ContextCompat.getDrawable(
            context,
            R.drawable.bg_all_rounded_lost_red
          )
          val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
          format.setTimeZone(TimeZone.getTimeZone("IST"));
          val date1: Date = format.parse(format.format(Date()))
          val date2: Date = format.parse(item.data.contractBiddingEndTime)
            if (date2.compareTo(date1) > 0) {
              val mills: Long = date2.getTime() - date1.getTime()
              stopCounter()
                countDownTimer = object : CountDownTimer(mills, 1000) {
                  override fun onTick(millisUntilFinished: Long) {
                    try {
                      val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
                      val mins = (millisUntilFinished / (1000 * 60)).toInt() % 60
                      val secs = ((millisUntilFinished / 1000).toInt() % 60).toLong()
                      val format = "%1$02d"
                      val hrs = String.format(format, hours)
                      val ms = String.format(format, mins)
                      val sec = String.format(format, secs)
                      val diff = ms + "m $sec" + "s"
                      if (hours == 0 && binding.tvBidStatus.text=="Live Bidding") {
                        binding.tvBidTime.visibility = View.VISIBLE
                        binding.tvBidTime.setText("Closes in $diff")
                      }

                    } catch (e: Exception) {
                      e.printStackTrace()
                    }
                  }

                  override fun onFinish() {
                    // Awaiting results after closing live bidding
                    binding.tvBidStatus.text = "Bidding Closed"
                    binding.tvBidStatus.setTextColor(
                      ContextCompat.getColor(
                        context,
                        R.color.dark_blue
                      )
                    )
                    binding.statusImage.setImageDrawable(
                      ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_awaiting_bid
                      )
                    )
                    binding.clBidStatus.background = ContextCompat.getDrawable(
                      context,
                      R.drawable.bg_all_rounded_under_review
                    )
                    binding.tvBidTime.setText(context.getString(R.string.awaiting_results))
                    // setting the user bid amount and status
                    if (item.data.transactionBid != null) {
                      binding.userBidStatus.text =
                        "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
                      binding.userBidInfo.text = "Your Bid"
                      binding.userBidInfo.setTextColor(
                        ContextCompat.getColor(
                          context,
                          R.color.heading_black
                        )
                      )
                      binding.userBidInfo.background = null
                      binding.userBidInfo.visibility = View.VISIBLE
                      binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                      )
                    } else {
                      // No user Bid
                      binding.userBidStatus.text = context.getString(string.you_did_not_bid)
                      binding.userBidInfo.visibility = View.GONE

                    }
                  }
                }.start()
            } else {
              //Awaiting Results after bidding closed
              binding.tvBidStatus.setTextColor(
                ContextCompat.getColor(
                  context,
                  R.color.dark_blue
                )
              )
              binding.statusImage.setImageDrawable(
                ContextCompat.getDrawable(
                  context,
                  R.drawable.ic_awaiting_bid
                )
              )
              binding.clBidStatus.background = ContextCompat.getDrawable(
                context,
                R.drawable.bg_all_rounded_under_review
              )
              binding.tvBidTime.setText(context.getString(R.string.awaiting_results))
            }
          //}
        } else {
          // open bidding but not live bidding
          binding.tvBidStatus.setTextColor(
            ContextCompat.getColor(
              context,
              R.color.pending_status
            )
          )
          binding.statusImage.setImageDrawable(
            ContextCompat.getDrawable(
              context,
              R.drawable.ic_money_pending
            )
          )
          binding.clBidStatus.background = ContextCompat.getDrawable(
            context,
            R.drawable.bg_all_rounded_pending
          )
          binding.tvBidTime.text = item.data.bidEndDate()

        }
        // setting the user bid amount and status
        if (item.data.transactionBid != null) {
           if(item.data.isContractBiddingOpen()&& item.data.isUnderOneHour()){
             binding.userBidInfo.visibility = View.VISIBLE
              }else{
                if(!item.data.isContractBiddingOpen()){
                  binding.userBidInfo.visibility = View.VISIBLE
                }else{
                  binding.userBidInfo.visibility = View.GONE
                }

              }
          if (item.data.lowestBid != null) {
            // user bid is lowest bid
            if (item.data.transactionBid?.bidAmount?.equals(item.data.lowestBid) == true) {
              binding.userBidStatus.text =
                "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
              binding.userBidInfo.setTextColor(
                ContextCompat.getColor(
                  context,
                  R.color.bid_placed_green
                )
              )
              binding.userBidInfo.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_rounded_won)
              binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_low_bid,
                0,
                0,
                0
              )
              binding.userBidInfo.text = " Your bid is the lowest"
            } else {
              // user bid is higher than lowest bid
              binding.userBidStatus.text =
                "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
              binding.userBidInfo.setTextColor(
                ContextCompat.getColor(
                  context,
                  R.color.destructive_red
                )
              )
              binding.userBidInfo.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_rounded_lost_red)
              binding.userBidInfo.text =
                "Higher than other by" + item.data.contractLowestbidDifference()
              binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
              )
            }
          }
        } else {
          // No user Bid
          binding.userBidStatus.text = context.getString(string.you_have_not_bid)
          binding.userBidInfo.setTextColor(ContextCompat.getColor(context, R.color.dark_blue))
          binding.userBidInfo.background = null
          binding.userBidInfo.text = context.getString(string.view_details)
          binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
           0,
            0,
            0,
            0
          )

        }

      }else{
        //Awaiting Results after bidding closed
        binding.tvBidStatus.setTextColor(
          ContextCompat.getColor(
            context,
            R.color.dark_blue
          )
        )
        binding.statusImage.setImageDrawable(
          ContextCompat.getDrawable(
            context,
            R.drawable.ic_awaiting_bid
          )
        )
        binding.clBidStatus.background = ContextCompat.getDrawable(
          context,
          R.drawable.bg_all_rounded_under_review
        )
        binding.tvBidTime.setText(context.getString(R.string.awaiting_results))
        // setting the user bid amount and status
        if (item.data.transactionBid != null) {
          binding.userBidStatus.text =
            "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
          binding.userBidInfo.text = "Your Bid"
          binding.userBidInfo.setTextColor(
            ContextCompat.getColor(
              context,
              R.color.heading_black
            )
          )
          binding.userBidInfo.background = null
          binding.userBidInfo.visibility = View.VISIBLE
          binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            0,
            0
          )
        } else {
          // No user Bid
          binding.userBidStatus.text = context.getString(string.you_did_not_bid)
          binding.userBidInfo.visibility = View.GONE

        }
      }

    }
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

