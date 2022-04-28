package com.delhivery.axle.ui.searchload.fragments.searchresults

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
      if(item.data.bidEndingTime.isNotNullOrEmpty()){
        item.data.bidEndingTime?.let { setTimer(it) }
      }
    }
    binding.btnBid.clickToAction(HomeBidsRequestAction_PlaceBid, item, adapterPosition, _interface)
    binding.viewBidInfo.clickToAction(
        HomeBidsRequestAction_PlaceBid, item, adapterPosition, _interface
    )
  }

  private fun setTimer(bidEndingTime: String) {
    val updateTimer = Timer()
    updateTimer.schedule(object : TimerTask() {
      override fun run() {
        try {
          val format = SimpleDateFormat("hh:mm:ss aa")
          val date1: Date = format.parse(format.format(Date()))
          val date2: Date = format.parse(bidEndingTime)
          val mills: Long = date1.getTime() - date2.getTime()
          val hours = (mills / (1000 * 60 * 60)).toInt()
          val mins = (mills / (1000 * 60)).toInt() % 60
          val secs = ((mills / 1000).toInt() % 60).toLong()
          val diff = "$hours:$mins:$secs" + "s" // updated value every1 second
          if(diff.equals("00:00:00s")){
            binding.timerLayout.visibility = View.GONE
          }else{
            binding.timerLayout.visibility = View.VISIBLE
            binding.timerTime.setText(diff)
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }, 0, 1000)
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