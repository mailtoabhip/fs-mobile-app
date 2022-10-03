package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.os.CountDownTimer
import android.text.TextUtils
import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
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