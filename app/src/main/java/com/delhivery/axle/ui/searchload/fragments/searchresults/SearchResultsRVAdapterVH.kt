package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_AcceptBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_NavigationMap
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.SUB_REQUEST_TYPE_INTRACITY
import com.delhivery.axle.databinding.CardCommonTripsBidsBinding
import com.delhivery.axle.databinding.ItemBottomCardLoadBinding
import com.delhivery.axle.databinding.LoadDelhiveryIntercityBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.home.fragments.bids.BaseHomeBidsRVAdapterViewHolder
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsProgressItem
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterInterface
import com.delhivery.axle.ui.home.fragments.contracts.BaseHomeContractsRVAdapterViewHolder
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsProgressItem
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterInterface
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.StringUtils.INTRACITY_CONTRACT_TYPE
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseSearchResultsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseSearchLoadsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: SearchLoadsRVAdapterInterface
  )

  var countDownTimer:CountDownTimer? = null

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: SearchLoadsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Add on click listener for action with position
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: SearchLoadsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: SearchLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }

  /**
   * Post action to UI with position
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    position: Int,
    _interface: SearchLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item, position) }

}

/**
 * Search request item view holder
 */
class SearchLoadsRequestItemVH(binding: LoadDelhiveryIntercityBinding) :
    BaseSearchResultsRVAdapterViewHolder<LoadDelhiveryIntercityBinding, SearchLoadsRequestItem>(
        binding
    ) {
  override fun bind(
    item: SearchLoadsRequestItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {

    if(item.data.subRequestType == SUB_REQUEST_TYPE_INTRACITY){
      binding.layoutIntracity.request = item.data
      binding.containerError.request = item.data
      binding.layoutIntracity.containerRowBidFor1.request= item.data
      binding.layoutIntracity.root.visibility = View.VISIBLE
      binding.cvIntercity.visibility = View.GONE
//      binding.layoutIntracity.layoutTransaction.navigate.visibility = View.VISIBLE
//      binding.layoutIntracity.layoutTransaction.navigate.clickToAction(HomeBidsRequestAction_NavigationMap, item, bindingAdapterPosition, _interface)
//      binding.layoutIntracity.btnAccept.clickToAction(HomeBidsRequestAction_AcceptBid, item, bindingAdapterPosition, _interface)
      binding.layoutIntracity.containerRowBidFor1.tvReportingTime.text = item.data.reportingTime?: item.data.requiredAtWithTime()
      val includedBinding = binding.layoutIntracity.containerRowBidFor1 as ItemBottomCardLoadBinding

      includedBinding.btnAcceptBid.clickToAction(HomeBidsRequestAction_AcceptBid, item, bindingAdapterPosition, _interface)

    } else {
      binding.layoutIntracity.root.visibility = View.GONE
      binding.cvIntercity.visibility = View.VISIBLE
      binding.request = item.data
      binding.containerError.request= item.data
      if(item.data.demandType.equals("Internal",true)){
        binding.materialType.visibility = View.GONE
      } else{
        binding.materialType.visibility = View.VISIBLE
        binding.materialType.text = item.data.materialType
      }
    if(item.data.isDMTIndent()){
      binding.closingTime.visibility = View.GONE
    }else{

      if(item.data.bidEndingTime.isNotNullOrEmpty() && item.data.transactionBid== null){
        binding.closingTime.visibility = View.VISIBLE
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        format.setTimeZone(TimeZone.getTimeZone("IST"));
        val date1: Date = format.parse(format.format(Date()))
        val date2: Date = format.parse(item.data.bidEndingTime)
//        if (date2.compareTo(date1) > 0) {
//          binding.closingTime.visibility = View.VISIBLE
//
//          val mills: Long = date2.getTime() - date1.getTime()
//          countDownTimer?.cancel()
//          countDownTimer = object : CountDownTimer(mills, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//              try {
//                val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
//                val mins = (millisUntilFinished / (1000 * 60)).toInt() % 60
//                val secs = ((millisUntilFinished / 1000).toInt() % 60).toLong()
//                val format = "%1$02d"
//                val hrs = String.format(format, hours)
//                val ms = String.format(format, mins)
//                val sec = String.format(format, secs)
//                val diff = "$hrs:$ms:$sec" + "s"
//                binding.timerTime.setText(diff)
//              } catch (e: Exception) {
//                e.printStackTrace()
//              }
//            }
//
//            override fun onFinish() {
//              _interface.deleteItem(item, bindingAdapterPosition)
//            }
//          }.start()
//        }else{
//          binding.closingTime.visibility = View.GONE
//        }

      }else{
        binding.closingTime.visibility = View.GONE
      }
    }


    if(item.data.indentOrigin.equals("LH")){
      if(item.data.indentHaltCenters.isNullOrEmpty()){
        binding.textStops.text = "No Stops"
      }else{
        binding.textStops.text = item.data.indentHaltCenters.size.toString()+" Stops"
      }
    }else{
      var total = 0
      if (!TextUtils.isEmpty(item.data.stop1City)) {
        total = total+1
      }
      if (!TextUtils.isEmpty(item.data.stop2City)) {
        total = total+1
      }

      if (!TextUtils.isEmpty(item.data.pickup1City)) {
        total = total+1
      }

      if (!TextUtils.isEmpty(item.data.pickup2City)) {
        total = total+1
      }

      if(total>0){
        binding.textStops.text = "$total Stops"
      }else{
        binding.textStops.text = "No Stops"
      }

    }
      Log.d("dataSearch","${item.data.reportingTime?:item.data?.requiredAtWithTime()}")
    binding.containerError.placeBidButton.clickToAction(HomeBidsRequestAction_ViewDetails, item, bindingAdapterPosition, _interface)
      binding.containerError.reportingTime.text = item.data.requiredAtWithTime()
//    binding.viewBidInfo.clickToAction(
//        HomeBidsRequestAction_PlaceBid, item, bindingAdapterPosition, _interface
//    )
  }
  }
}

/**
 * Search load dummy header
 */
internal class SearchLoadsSearchSpinnerItemVH(binding: ViewHomeBidsSearchSpinnerItemBinding) :
    BaseSearchResultsRVAdapterViewHolder<ViewHomeBidsSearchSpinnerItemBinding, SearchLoadsSearchSpinnerItem>(
        binding
    ) {
  override fun bind(
    item: SearchLoadsSearchSpinnerItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {
    binding.isIntracity=item.data.isIntracity
  }
}

/**
 * Contracts request item view holder
 */
class SearchContractsRequestItemVH(binding: CardCommonTripsBidsBinding) :
  BaseSearchResultsRVAdapterViewHolder<CardCommonTripsBidsBinding, SearchContractsRequestItem>(
    binding
  ) {

  override fun bind(
    item: SearchContractsRequestItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {
    binding.request = item.data

    binding.request = item.data
    //hide contract and live bidding tags view
    binding.includeHeader1.includeHeader1.visibility = View.GONE
    //hide 7 days a week view
    binding.includeBidTime3.tvFrequency.visibility = View.GONE
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
    binding.includeBidTime6.placeBidButton.visibility = View.VISIBLE
//    if(item.data.isItIntraCityContract()){
//      if(item.data.isFlexible){
//        binding.tvIntracityHubOriginCity.text = if(item.data.secondaryReportingCenters!=null&&item.data.secondaryReportingCenters.size>1){
//          HtmlCompat.fromHtml(context.getString(R.string.msg_more_reporting_centers,item.data.reportingCenters(),item.data.secondaryReportingCenters.size-1), HtmlCompat.FROM_HTML_MODE_LEGACY)}else item.data.reportingCenters()
//        binding.intracityContractTypeChip.setCompoundDrawablesWithIntrinsicBounds(
//          R.drawable.ic_multiple_location,
//          0,
//          0,
//          0
//        )
//      }else{
//        binding.intracityContractTypeChip.setCompoundDrawablesWithIntrinsicBounds(
//          R.drawable.ic_place,
//          0,
//          0,
//          0
//        )
//        binding.tvIntracityHubOriginCity.text =item.data.originCityName()
//      }
//    }else{
//      binding.tvHubOriginCity.text = item.data.originCityName()
//    }
//    if (item.data.contractType == INTRACITY_CONTRACT_TYPE) {
//      binding.viewKmsPerMonth.text=item.data.intracityKms.plus(" Kms/Month")
//      binding.viewDaysPerMonth.text=item.data.intracityDays.plus(" days/Month")
//      binding.viewHoursPerDay.text=item.data.intracityHours.plus("h/day")
//    }
//    //Transaction Cancelled
//    if (item.data.transactionStatus == "cancelled") {
//      binding.userBidInfo.visibility = View.GONE
//      binding.tvBidStatus.text = "Cancelled"
//      binding.tvBidTime.visibility = View.GONE
//      binding.tvBidStatus.setTextColor(
//        ContextCompat.getColor(
//          context,
//          R.color.heading_black
//        )
//      )
//      binding.statusImage.setImageDrawable(
//        ContextCompat.getDrawable(
//          context,
//          R.drawable.ic_cancel_contract
//        )
//      )
//      binding.clBidStatus.background = ContextCompat.getDrawable(
//        context,
//        R.drawable.bg_all_round_corner_light_grey
//      )
//    } else if (item.data.bidStatus() == Accepted || item.data.bidStatus() == Cancelled || item.data.bidStatus() == Rejected) {
//      binding.userBidInfo.visibility = View.VISIBLE
//      //Result status
//      binding.tvBidStatus.setTextColor(
//        ContextCompat.getColor(
//          context,
//          R.color.bid_placed_green
//        )
//      )
//      binding.statusImage.setImageDrawable(
//        ContextCompat.getDrawable(
//          context,
//          R.drawable.ic_bid_result
//        )
//      )
//      binding.clBidStatus.background = ContextCompat.getDrawable(
//        context,
//        R.drawable.bg_all_rounded_won
//      )
//      binding.tvBidTime.visibility = View.GONE
//      if (item.data.bidStatus() == Accepted) {
//        //Bid Won
//        binding.userBidStatus.text =
//          "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
//        binding.userBidInfo.text = " You Won"
//        binding.userBidInfo.background = null
//        binding.userBidInfo.setTextColor(
//          ContextCompat.getColor(
//            context,
//            R.color.background_dark_grey
//          )
//        )
//        binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
//          R.drawable.ic_thumbs_up_green,
//          0,
//          0,
//          0
//        )
//      } else if (item.data.bidStatus() == Rejected) {
//        //Bid Lost
//        binding.userBidStatus.text =
//          "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
//        binding.userBidInfo.text = " You Lost"
//        binding.userBidInfo.background = null
//        binding.userBidInfo.setTextColor(
//          ContextCompat.getColor(
//            context,
//            R.color.background_dark_grey
//          )
//        )
//        binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
//          R.drawable.ic_thumbs_down,
//          0,
//          0,
//          0
//        )
//      }
//    } else if (item.data.transactionStatus == "allocated") {
//      binding.userBidInfo.visibility = View.GONE
//      //Result status
//      binding.tvBidStatus.setTextColor(
//        ContextCompat.getColor(
//          context,
//          R.color.bid_placed_green
//        )
//      )
//      binding.statusImage.setImageDrawable(
//        ContextCompat.getDrawable(
//          context,
//          R.drawable.ic_bid_result
//        )
//      )
//      binding.clBidStatus.background = ContextCompat.getDrawable(
//        context,
//        R.drawable.bg_all_rounded_won
//      )
//      binding.tvBidTime.visibility = View.GONE
//      binding.userBidStatus.text = "You did not bid"
//    } else {
//      binding.tvBidTime.visibility = View.VISIBLE
//      //Bidding open
//      if (item.data.isContractBiddingOpen()) {
//        binding.userBidInfo.visibility = View.VISIBLE
//        // Live bidding
//        if (item.data.isUnderOneHour()) {
//          binding.clBidStatus.visibility = View.VISIBLE
//          binding.tvBidStatus.text = "Live Bidding"
//          binding.tvBidStatus.setTextColor(
//            ContextCompat.getColor(
//              context,
//              R.color.destructive_red
//            )
//          )
//          GlideApp.with(context)
//            .load(R.raw.livebidding)
//            .apply(RequestOptions().override(40, 40))
//            .into(binding.statusImage)
//
//          binding.clBidStatus.background = ContextCompat.getDrawable(
//            context,
//            R.drawable.bg_all_rounded_lost_red
//          )
//          val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
//          format.setTimeZone(TimeZone.getTimeZone("IST"));
//          val date1: Date = format.parse(format.format(Date()))
//          val date2: Date = format.parse(item.data.contractBiddingEndTime)
//            if (date2.compareTo(date1) > 0) {
//              val mills: Long = date2.getTime() - date1.getTime()
//              stopCounter()
//              countDownTimer = object : CountDownTimer(mills, 1000) {
//                override fun onTick(millisUntilFinished: Long) {
//                  try {
//                    val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
//                    val mins = (millisUntilFinished / (1000 * 60)).toInt() % 60
//                    val secs = ((millisUntilFinished / 1000).toInt() % 60).toLong()
//                    val format = "%1$02d"
//                    val hrs = String.format(format, hours)
//                    val ms = String.format(format, mins)
//                    val sec = String.format(format, secs)
//                    val diff = ms + "m $sec" + "s"
//                    if (hours == 0 && binding.tvBidStatus.text == "Live Bidding") {
//                      binding.tvBidTime.visibility = View.VISIBLE
//                      binding.tvBidTime.setText("Closes in $diff")
//                    }
//
//                  } catch (e: Exception) {
//                    e.printStackTrace()
//                  }
//                }
//
//                override fun onFinish() {
//                  // Awaiting results after closing live bidding
//                  binding.tvBidStatus.text = "Bidding Closed"
//                  binding.tvBidStatus.setTextColor(
//                    ContextCompat.getColor(
//                      context,
//                      R.color.dark_blue
//                    )
//                  )
//                  binding.statusImage.setImageDrawable(
//                    ContextCompat.getDrawable(
//                      context,
//                      R.drawable.ic_awaiting_bid
//                    )
//                  )
//                  binding.clBidStatus.background = ContextCompat.getDrawable(
//                    context,
//                    R.drawable.bg_all_rounded_under_review
//                  )
//                  binding.tvBidTime.setText(context.getString(R.string.awaiting_results))
//                  // setting the user bid amount and status
//                  if (item.data.transactionBid != null) {
//                    binding.userBidStatus.text =
//                      "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
//                    binding.userBidInfo.text = "Your Bid"
//                    binding.userBidInfo.setTextColor(
//                      ContextCompat.getColor(
//                        context,
//                        R.color.heading_black
//                      )
//                    )
//                    binding.userBidInfo.background = null
//                    binding.userBidInfo.visibility = View.VISIBLE
//                    binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
//                      0,
//                      0,
//                      0,
//                      0
//                    )
//                  } else {
//                    // No user Bid
//                    binding.userBidStatus.text = context.getString(string.you_did_not_bid)
//                    binding.userBidInfo.visibility = View.GONE
//
//                  }
//                }
//              }.start()
//            } else {
//              //Awaiting Results after bidding closed
//              binding.tvBidStatus.setTextColor(
//                ContextCompat.getColor(
//                  context,
//                  R.color.dark_blue
//                )
//              )
//              binding.statusImage.setImageDrawable(
//                ContextCompat.getDrawable(
//                  context,
//                  R.drawable.ic_awaiting_bid
//                )
//              )
//              binding.clBidStatus.background = ContextCompat.getDrawable(
//                context,
//                R.drawable.bg_all_rounded_under_review
//              )
//              binding.tvBidTime.setText(context.getString(R.string.awaiting_results))
//            }
//        } else {
//          // open bidding but not live bidding
//          binding.tvBidStatus.setTextColor(
//            ContextCompat.getColor(
//              context,
//              R.color.pending_status
//            )
//          )
//          binding.statusImage.setImageDrawable(
//            ContextCompat.getDrawable(
//              context,
//              R.drawable.ic_money_pending
//            )
//          )
//          binding.clBidStatus.background = ContextCompat.getDrawable(
//            context,
//            R.drawable.bg_all_rounded_pending
//          )
//          binding.tvBidTime.text = item.data.bidEndDate()
//
//        }
//        // setting the user bid amount and status
//        if (item.data.transactionBid != null) {
//          if (item.data.isContractBiddingOpen() && item.data.isUnderOneHour()) {
//            binding.userBidInfo.visibility = View.VISIBLE
//          } else {
//            if (!item.data.isContractBiddingOpen()) {
//              binding.userBidInfo.visibility = View.VISIBLE
//            } else {
//              binding.userBidInfo.visibility = View.GONE
//            }
//
//          }
//          if (item.data.lowestBid != null) {
//            // user bid is lowest bid
//            if (item.data.transactionBid?.bidAmount?.equals(item.data.lowestBid) == true ) {
//              binding.userBidStatus.text =
//                "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
//              binding.userBidInfo.setTextColor(
//                ContextCompat.getColor(
//                  context,
//                  R.color.bid_placed_green
//                )
//              )
//              binding.userBidInfo.background =
//                ContextCompat.getDrawable(context, R.drawable.bg_all_rounded_won)
//              binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
//                R.drawable.ic_low_bid,
//                0,
//                0,
//                0
//              )
//              binding.userBidInfo.text = " Your bid is the lowest "
//            } else {
//              // user bid is higher than lowest bid
//              binding.userBidStatus.text =
//                "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
//              binding.userBidInfo.setTextColor(
//                ContextCompat.getColor(
//                  context,
//                  R.color.destructive_red
//                )
//              )
//              binding.userBidInfo.background =
//                ContextCompat.getDrawable(context, R.drawable.bg_all_rounded_lost_red)
//              binding.userBidInfo.text =
//                "Higher than other by" + item.data.contractLowestbidDifference()
//              binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
//                0,
//                0,
//                0,
//                0
//              )
//            }
//          }
//        } else {
//          // No user Bid
//          binding.userBidStatus.text = context.getString(string.you_have_not_bid)
//          binding.userBidInfo.setTextColor(ContextCompat.getColor(context, R.color.dark_blue))
//          binding.userBidInfo.background = null
//          binding.userBidInfo.text = context.getString(string.view_details)
//          binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
//            0,
//            0,
//            0,
//            0
//          )
//
//        }
//
//      } else {
//        //Awaiting Results after bidding closed
//        binding.tvBidStatus.setTextColor(
//          ContextCompat.getColor(
//            context,
//            R.color.dark_blue
//          )
//        )
//        binding.statusImage.setImageDrawable(
//          ContextCompat.getDrawable(
//            context,
//            R.drawable.ic_awaiting_bid
//          )
//        )
//        binding.clBidStatus.background = ContextCompat.getDrawable(
//          context,
//          R.drawable.bg_all_rounded_under_review
//        )
//        binding.tvBidTime.setText(context.getString(R.string.awaiting_results))
//        // setting the user bid amount and status
//        if (item.data.transactionBid != null) {
//          binding.userBidStatus.text =
//            "₹" + StringUtils.formatAmount(item.data?.transactionBid?.bidAmount!!)
//          binding.userBidInfo.text = "Your Bid"
//          binding.userBidInfo.setTextColor(
//            ContextCompat.getColor(
//              context,
//              R.color.heading_black
//            )
//          )
//          binding.userBidInfo.background = null
//          binding.userBidInfo.visibility = View.VISIBLE
//          binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
//            0,
//            0,
//            0,
//            0
//          )
//        } else {
//          // No user Bid
//          binding.userBidStatus.text = context.getString(string.you_did_not_bid)
//          binding.userBidInfo.visibility = View.GONE
//
//        }
//      }

//    }
  }
//  fun stopCounter() {
//    countDownTimer?.cancel()
//    countDownTimer = null
//  }
}

internal class SearchContractsProgressItemVH(binding: ViewHomeContractsProgressItemBinding) :
  BaseSearchResultsRVAdapterViewHolder<ViewHomeContractsProgressItemBinding, SearchContractsProgressItem>(
    binding
  ) {
  override fun bind(
    item: SearchContractsProgressItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {

  }
}

internal class SearchLoadsProgressItemVH(binding: ViewHomeBidsProgressItemBinding) :
  BaseSearchResultsRVAdapterViewHolder<ViewHomeBidsProgressItemBinding, SearchLoadsProgressItem>(
    binding
  ) {
  override fun bind(
    item: SearchLoadsProgressItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {

  }
}
/**
 * Search load warning item
 */
internal class SearchLoadsWarningItemVH(binding: ViewWarningItemBinding) :
    BaseSearchResultsRVAdapterViewHolder<ViewWarningItemBinding, SearchLoadsWarningItem>(
        binding
    ) {
  override fun bind(
    item: SearchLoadsWarningItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.btnAction.visibility = View.GONE
  }
}