package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.os.CountDownTimer
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.home.fragments.contracts.BaseHomeContractsRVAdapterViewHolder
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterInterface
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRequestItem
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import java.text.SimpleDateFormat
import java.util.*

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
class SearchLoadsRequestItemVH(binding: ViewHomeLoadsRequestItemBinding) :
    BaseSearchResultsRVAdapterViewHolder<ViewHomeLoadsRequestItemBinding, SearchLoadsRequestItem>(
        binding
    ) {
  override fun bind(
    item: SearchLoadsRequestItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {
    binding.request = item.data

    if(item.data.isDMTIndent()){
      binding.timerLayout.visibility = View.GONE
    }else{
      if(item.data.bidEndingTime.isNotNullOrEmpty() && item.data.transactionBid== null){
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        format.setTimeZone(TimeZone.getTimeZone("IST"));
        val date1: Date = format.parse(format.format(Date()))
        val date2: Date = format.parse(item.data.bidEndingTime)
        if (date2.compareTo(date1) > 0) {
          binding.timerLayout.visibility = View.VISIBLE

          val mills: Long = date2.getTime() - date1.getTime()
          countDownTimer?.cancel()
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
                val diff = "$hrs:$ms:$sec" + "s"
                binding.timerTime.setText(diff)
              } catch (e: Exception) {
                e.printStackTrace()
              }
            }

            override fun onFinish() {
              _interface.deleteItem(item, adapterPosition)
            }
          }.start()
        }else{
          binding.timerLayout.visibility = View.GONE
        }

      }else{
        binding.timerLayout.visibility = View.GONE
      }
    }


    if(item.data.indentOrigin.equals("LH")){
      if(item.data.indentHaltCenters.isNullOrEmpty()){
        binding.stopNo.text = "No Stops"
      }else{
        binding.stopNo.text = item.data.indentHaltCenters.size.toString()+" Stops"
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
        binding.stopNo.text = "$total Stops"
      }else{
        binding.stopNo.text = "No Stops"
      }

    }

    binding.btnBid.clickToAction(HomeBidsRequestAction_PlaceBid, item, adapterPosition, _interface)
    binding.viewBidInfo.clickToAction(
        HomeBidsRequestAction_PlaceBid, item, adapterPosition, _interface
    )
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

  }
}

/**
 * Contracts request item view holder
 */
class SearchContractsRequestItemVH(binding: ViewHomeContractsRequestItemBinding) :
  BaseSearchResultsRVAdapterViewHolder<ViewHomeContractsRequestItemBinding, SearchContractsRequestItem>(
    binding
  ) {

  override fun bind(
    item: SearchContractsRequestItem,
    _interface: SearchLoadsRVAdapterInterface
  ) {
    binding.request = item.data
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
    }else if (item.data.bidStatus() == TransactionBidStatus.Accepted || item.data.bidStatus() == TransactionBidStatus.Cancelled || item.data.bidStatus() == TransactionBidStatus.Rejected) {
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
      if (item.data.bidStatus() == TransactionBidStatus.Accepted) {
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
      } else if (item.data.bidStatus() == TransactionBidStatus.Rejected) {
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
          synchronized(this) {


            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            format.setTimeZone(TimeZone.getTimeZone("IST"));
            val date1: Date = format.parse(format.format(Date()))
            val date2: Date = format.parse(item.data.contractBiddingEndTime)
            if (date2.compareTo(date1) > 0) {
              val mills: Long = date2.getTime() - date1.getTime()
              countDownTimer?.cancel()
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
                    binding.userBidStatus.text = context.getString(R.string.you_have_not_bid)
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
          }
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
            if (item.data.transactionBid?.bidAmount?.equals(item.data.lowestBid) == true && (item.data.targetPrice==null||item.data.transactionBid?.bidAmount?:0.0<=item.data.targetPrice?:0.0)) {
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
          }else{
            if(item.data.targetPrice!=null){
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
          binding.userBidStatus.text = context.getString(R.string.you_have_not_bid)
          binding.userBidInfo.setTextColor(ContextCompat.getColor(context, R.color.dark_blue))
          binding.userBidInfo.background = null
          binding.userBidInfo.text = context.getString(R.string.view_details)
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
          binding.userBidStatus.text = context.getString(R.string.you_have_not_bid)
          binding.userBidInfo.visibility = View.GONE

        }
      }

    }
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