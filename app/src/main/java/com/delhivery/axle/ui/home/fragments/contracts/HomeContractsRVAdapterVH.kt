package com.delhivery.axle.ui.home.fragments.contracts

import android.os.CountDownTimer
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.contracts.HomeContractsFilterCustomerIntercity
import com.delhivery.axle.data.home.contracts.HomeContractsFilterDLVIntercity
import com.delhivery.axle.data.home.contracts.HomeContractsFilterDLVIntracity
import com.delhivery.axle.data.home.contracts.HomeContractsFilterInfo
import com.delhivery.axle.data.home.contracts.HomeContractsFilterItemData
import com.delhivery.axle.databinding.ViewFilterInfoItemBinding
import com.delhivery.axle.databinding.ViewFilterToggleItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsSearchItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.utils.StringUtils
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
                    binding.userBidStatus.text = context.getString(string.you_have_not_bid)
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
          binding.userBidStatus.text = context.getString(string.you_have_not_bid)
          binding.userBidInfo.visibility = View.GONE

        }
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
* Search item view holder
*/
internal class HomeContractsSearchItemVH(binding: ViewHomeLoadsSearchItemBinding) :
  BaseHomeContractsRVAdapterViewHolder<ViewHomeLoadsSearchItemBinding, HomeContractsSearchItem>(
    binding
  ) {
  override fun bind(
    item: HomeContractsSearchItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
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
    val filterRVAdapter=ContractsFilterRVAdapter(_interface)
    binding.filterRv.apply {
      layoutManager=LinearLayoutManager(this.context,LinearLayoutManager.HORIZONTAL,false)
      adapter=filterRVAdapter
    }
    filterRVAdapter.operation(
      FilterToggleItem(HomeContractsFilterItemData("CustomerIntercityToggle",item.data.actionLabel,item.data.expressCount,item.data.nonExpressCount,item.data.intracityCount,item.data.userDemandType)),
      Add
    )
    filterRVAdapter.operation(
      FilterToggleItem(HomeContractsFilterItemData("DLVIntercityToggle",item.data.actionLabel,item.data.expressCount,item.data.nonExpressCount,item.data.intracityCount,item.data.userDemandType)),
      Add
    )
    filterRVAdapter.operation(
      FilterToggleItem(HomeContractsFilterItemData("DLVIntracityToggle",item.data.actionLabel,item.data.expressCount,item.data.nonExpressCount,item.data.intracityCount,item.data.userDemandType)),
      Add
    )
    filterRVAdapter.operation(FilterInfoItem(),Add)
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

internal class FilterToggleItemVH(binding: ViewFilterToggleItemBinding):
  BaseHomeContractsRVAdapterViewHolder<ViewFilterToggleItemBinding,FilterToggleItem>(binding){
  override fun bind(item: FilterToggleItem, _interface: HomeContractsRVAdapterInterface) {
    when (item.data.itemType) {
      "CustomerIntercityToggle" -> {
        binding.toggleView.text="Customer Intercity (${item.data.nonExpressCount})"
        if (!item.data.actionLabel && !item.data.userDemandType.contains("Intracity")) {
          binding.toggleView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
          binding.toggleView.isSelected=true
        } else {
          binding.toggleView.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
          binding.toggleView.isSelected=false
        }
        if(item.data.userDemandType.contains("Internal") || item.data.userDemandType.contains("Corporate"))
          binding.toggleView.visibility=View.VISIBLE
        else
          binding.toggleView.visibility=View.GONE
        binding.toggleView.clickToAction(HomeContractsFilterCustomerIntercity, item, _interface)
      }
      "DLVIntercityToggle" -> {
        binding.toggleView.text="Delhivery Line Haul (${item.data.expressCount})"
        if(item.data.userDemandType.contains("Internal"))
          binding.toggleView.visibility=View.VISIBLE
        else binding.toggleView.visibility=View.GONE
        if (!item.data.actionLabel) {
          binding.toggleView.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
          binding.toggleView.isSelected = false
        } else if(!item.data.userDemandType.contains("Intracity")){
          binding.toggleView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
          binding.toggleView.isSelected = true
        }
        binding.toggleView.clickToAction(HomeContractsFilterDLVIntercity, item, _interface)
      }
      else -> {
        binding.toggleView.text="Delhivery Fleet (${item.data.intracityCount})"
        if(item.data.userDemandType.contains("Intracity")){
          binding.toggleView.visibility=View.VISIBLE
          binding.toggleView.setTextColor(ContextCompat.getColor(context,R.color.colorAccent))
          binding.toggleView.isSelected=true
        }
        else {
          binding.toggleView.visibility=View.INVISIBLE
          binding.toggleView.setTextColor(ContextCompat.getColor(context,R.color.background_dark_grey))
          binding.toggleView.isSelected=false
        }
        binding.toggleView.clickToAction(HomeContractsFilterDLVIntracity,item,_interface)
      }
    }
  }

}

internal class FilterInfoItemVH(binding: ViewFilterInfoItemBinding):
  BaseHomeContractsRVAdapterViewHolder<ViewFilterInfoItemBinding,FilterInfoItem>(binding){
  override fun bind(item: FilterInfoItem, _interface: HomeContractsRVAdapterInterface) {
    binding.info.clickToAction(HomeContractsFilterInfo, item, _interface)
  }
}

