package com.delhivery.axle.ui.home.fragments.contracts

import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.R.color
import com.delhivery.axle.R.string
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.contracts.HomeContractsFilterExpress
import com.delhivery.axle.data.home.contracts.HomeContractsFilterInfo
import com.delhivery.axle.data.home.contracts.HomeContractsFilterNonExpress
import com.delhivery.axle.data.home.loads.HomeLoadsBannerAction
import com.delhivery.axle.data.home.loads.HomeLoadsFilterAction
import com.delhivery.axle.data.home.loads.HomeLoadsInfoAction_EditRoute
import com.delhivery.axle.data.home.loads.HomeLoadsInfoAction_Search
import com.delhivery.axle.data.home.loads.HomeLoadsPriorityAction
import com.delhivery.axle.data.home.loads.HomeLoadsShareRateAction
import com.delhivery.axle.data.home.loads.HomeLoadsVehicleFilterAction
import com.delhivery.axle.databinding.ViewHomeContractsFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsInfoItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsMoreInfoItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsSearchItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsTruckBannerItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsTruckPriorityItemBinding
import com.delhivery.axle.databinding.ViewHomeSummaryItemBinding
import com.delhivery.axle.databinding.ViewShareLayoutBannerBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.home.fragments.loads.BaseHomeLoadsRVAdapterViewHolder
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsAddTruckItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFilterItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsInfoItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsMoreInfoItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsProgressItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterInterface
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRequestItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsSearchItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsShareRateItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsSummaryItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsTimeoutItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsTruckPriorityAccessItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsWarningItem
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import java.text.SimpleDateFormat
import java.util.Date
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
        binding.userBidInfo.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_thumbs_down,
          0,
          0,
          0
        )
      }
    } else {
      binding.tvBidTime.visibility = View.VISIBLE
      //Bidding open
      if (item.data.isContractBiddingOpen()) {
        binding.userBidInfo.visibility= View.VISIBLE
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
                    }else{
                      if(binding.tvBidStatus.text=="Collecting Bids"){
                        binding.tvBidTime.visibility = View.VISIBLE
                         if (item.data.bidCollectionSlot == "morning") {
                             binding.tvBidTime.setText(context.getString(R.string.live_11_12)) }
                         else {
                                binding.tvBidTime.setText(context.getString(R.string.live_4_5))
                              }
                      }else if(binding.tvBidStatus.text=="Bidding Closed"){
                        binding.tvBidTime.setText(context.getString(R.string.awaiting_results))
                      }else{
                        binding.tvBidTime.visibility = View.GONE
                      }

                    }

                  } catch (e: Exception) {
                    e.printStackTrace()
                  }
                }

                override fun onFinish() {
                  // Awaiting results
                  binding.tvBidStatus.text = context.getString(R.string.collecting_bids)
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
              }.start()
            } else {
              //Awaiting Results
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

        } else {
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
          if (item.data.bidCollectionSlot == "morning") {
            binding.tvBidTime.setText(context.getString(R.string.live_11_12))
          } else {
            binding.tvBidTime.setText(context.getString(R.string.live_4_5))
          }
        }

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
                "Higher than other by ₹" + item.data.contractLowestbidDifference()

            }
          }
        } else {
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
        //Bidding closed, set awaiting for result
        binding.userBidStatus.text= context.getString(string.you_did_not_bid)
        binding.userBidInfo.visibility= View.GONE
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
  }
}

/**
 * Progress inline view holder
 */
internal class HomeContractsProgressItemVH(binding: ViewHomeLoadsProgressItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewHomeLoadsProgressItemBinding, HomeContractsProgressItem>(
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
 * Loads filter view holder
 */
internal class HomeContractsFilterItemVH(binding: ViewHomeContractsFilterItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewHomeContractsFilterItemBinding, HomeContractsFilterItem>(binding) {
  override fun bind(
    item: HomeContractsFilterItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
     binding.nonExpressToggle.text =  "Non Express Load (${item.data.nonExpressCount})"
     binding.expressToggle.text =   "Express Load (${item.data.expressCount})"
      if(item.data.userDemandType.contains("Internal")&&item.data.userDemandType.contains("Others") ){
        binding.expressToggle.visibility = View.VISIBLE
        binding.nonExpressToggle.visibility = View.VISIBLE
      }else{
        if(item.data.userDemandType.contains("Internal")){
          binding.expressToggle.visibility = View.VISIBLE
          binding.nonExpressToggle.visibility = View.GONE
        }else{
          binding.expressToggle.visibility = View.GONE
          binding.nonExpressToggle.visibility = View.VISIBLE
        }

      }
    if (!item.data.actionLabel) {
      binding.nonExpressToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
      binding.expressToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
      binding.expressToggle.isSelected = false
      binding.nonExpressToggle.isSelected=true
    } else {
      binding.expressToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
      binding.nonExpressToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
      binding.expressToggle.isSelected = true
      binding.nonExpressToggle.isSelected=false
    }

    binding.expressToggle.clickToAction(HomeContractsFilterExpress, item, _interface)
    binding.nonExpressToggle.clickToAction(HomeContractsFilterNonExpress, item, _interface)
    binding.info.clickToAction(HomeContractsFilterInfo, item, _interface)
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

