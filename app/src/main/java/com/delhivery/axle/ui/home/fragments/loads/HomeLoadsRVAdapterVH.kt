package com.delhivery.axle.ui.home.fragments.loads

import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_AcceptBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_NavigationMap
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.bids.SUB_REQUEST_TYPE_INTRACITY
import com.delhivery.axle.data.home.loads.*
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import java.text.SimpleDateFormat
import java.util.*

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseHomeLoadsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeLoadsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
          item: IT,
          _interface: HomeLoadsRVAdapterInterface
  )

  var countDownTimer:CountDownTimer? = null

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
          actionId: String,
          item: IT,
          _interface: HomeLoadsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Add on click listener for action with position
   */
  protected fun View.clickToAction(
          actionId: String,
          item: IT,
          position: Int,
          _interface: HomeLoadsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
          actionId: String,
          item: IT,
          _interface: HomeLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }

  /**
   * Post action to UI with position
   */
  protected fun View.action(
          actionId: String,
          item: IT,
          position: Int,
          _interface: HomeLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item, position) }
}

/**
 * Bid request item view holder
 */
class HomeLoadsRequestItemVH(binding: LoadDelhiveryIntercityBinding) :
    BaseHomeLoadsRVAdapterViewHolder<LoadDelhiveryIntercityBinding, HomeLoadsRequestItem>(
            binding
    ) {

  override fun bind(
          item: HomeLoadsRequestItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {

      Log.d("adapterData", "${item.data}")

    if(item.data.subRequestType == SUB_REQUEST_TYPE_INTRACITY){
        binding.layoutIntracity.request = item.data
        binding.layoutIntracity.containerRowBidFor1.request = item.data
        binding.layoutIntracity.root.visibility = View.VISIBLE
        binding.cvIntercity.visibility = View.GONE
//        binding.layoutIntracity.containerRowBidFor1.placeBidButton1.rootView.
//        binding.nonIntracityLayout.visibility = View.GONE
//        binding.layoutIntracity.layoutTransaction.navigate.visibility = View.VISIBLE
//        binding.layoutIntracity.containerRowBidFor1.placeBidButton1.clickToAction(HomeBidsRequestAction_NavigationMap, item, bindingAdapterPosition, _interface)
        binding.layoutIntracity.containerRowBidFor1.tvReportingTime.text = item.data.reportingTime?: item.data.requiredAtWithTime()
        val includedBinding = binding.layoutIntracity.containerRowBidFor1 as ItemBottomCardLoadBinding
        includedBinding.placeBidButton1.rootView.setOnClickListener {
            Log.d("Intracity Card Clicked", "rjrfv")
            it.clickToAction(HomeBidsRequestAction_AcceptBid, item, bindingAdapterPosition, _interface)
        }
//        binding.layoutIntracity.containerRowBidFor1.placeBidButton1.setOnClickListener {
//            Log.d("Intracity Card Clicked", "rjrfv")
//            it.clickToAction(HomeBidsRequestAction_AcceptBid, item, bindingAdapterPosition, _interface)
//        }

//        binding.layoutIntracity.containerRowBidFor1.placeBidButton.clickToAction(HomeBidsRequestAction_AcceptBid, item, bindingAdapterPosition, _interface)
    } else {
        binding.layoutIntracity.root.visibility = View.GONE
        binding.cvIntercity.visibility = View.VISIBLE
//        binding.nonIntracityLayout.visibility = View.VISIBLE
        binding.request = item.data
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
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                format.setTimeZone(TimeZone.getTimeZone("IST"));
                val date1: Date = format.parse(format.format(Date()))
                val date2: Date = format.parse(item.data.bidEndingTime)
                if (date2.compareTo(date1) > 0) {
                    binding.closingTime.visibility = View.VISIBLE
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
                                binding.closingTime.setText(diff)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onFinish() {
                            _interface.deleteItem(item, bindingAdapterPosition)
                        }
                    }.start()
                }else{
                    binding.closingTime.visibility = View.GONE
                }

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

        binding.containerError.placeBidButton.clickToAction(HomeBidsRequestAction_PlaceBid, item, bindingAdapterPosition, _interface)
        binding.containerError.reportingTime.text = item.data.reportingTime?: item.data.requiredAtWithTime()
    }

//    binding.viewBidInfo.clickToAction(HomeBidsRequestAction_PlaceBid, item, bindingAdapterPosition, _interface
//    )
  }

}

/**
 * Progress inline view holder
 */
internal class HomeLoadsProgressItemVH(binding: ViewHomeLoadsProgressItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsProgressItemBinding, HomeLoadsProgressItem>(
            binding
    ) {
  override fun bind(
          item: HomeLoadsProgressItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
  }
}

/**
 * Search item view holder
 */
internal class HomeLoadsSearchItemVH(binding: ViewHomeLoadsSearchPlaceholderItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsSearchPlaceholderItemBinding, HomeLoadsSearchItem>(
            binding
    ) {
  override fun bind(
          item: HomeLoadsSearchItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.editStickySearch.clickToAction(HomeLoadsSearchAction_Search,item,_interface)
    binding.spinnerTruckDisplayName.clickToAction(HomeLoadsVehicleFilterAction,item,_interface)
    binding.spinnerTruckDisplayName.text = "Vehicle Type"+if(item.data.query?.split(",")?.size==0)"" else " : " + item.data.query?.split(",")?.joinToString(", ")
  }
}

/**
 * Bids warning item view holder
 */
internal class HomeLoadsWarningItemVH(binding: ViewWarningItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewWarningItemBinding, HomeLoadsWarningItem>(
            binding
    ) {
  override fun bind(
          item: HomeLoadsWarningItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 *  Load Add Truck item view holder
 */
internal class HomeLoadsAddTruckItemVH(binding: ViewHomeLoadsTruckBannerItemBinding) :
  BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsTruckBannerItemBinding, HomeLoadsAddTruckItem>(
          binding
  ) {
  override fun bind(
          item: HomeLoadsAddTruckItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
      binding.layoutBanner.clickToAction(HomeLoadsBannerAction, item, _interface)
  }
}

/**
 *   Add Truck priority item view holder
 */
internal class HomeLoadsTruckPriorityItemVH(binding: ViewHomeLoadsTruckPriorityItemBinding) :
  BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsTruckPriorityItemBinding, HomeLoadsTruckPriorityAccessItem>(
          binding
  ) {
  override fun bind(
          item: HomeLoadsTruckPriorityAccessItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
      binding.truckBanner.clickToAction(HomeLoadsPriorityAction, item, _interface)
  }
}


internal class HomeLoadsShareRateItemVH(binding: ViewShareLayoutBannerBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewShareLayoutBannerBinding, HomeLoadsShareRateItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsShareRateItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.title= item.data.title
    binding.actionLabel=item.data.rate
    binding.subTitle=item.data.subTitle
    binding.shareButton.clickToAction(HomeLoadsShareRateAction, item, _interface)
  }
}

internal class HomeLoadsCategoriesItemVH(binding: ViewHomeLoadCategoriesItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadCategoriesItemBinding, HomeLoadsCategoriesItem>(
        binding
    ) {
    override fun bind(
        item: HomeLoadsCategoriesItem,
        _interface: HomeLoadsRVAdapterInterface
    ) {
        val infoText  = when (item.data.textDesc) {
            DemandType.Intracity.type-> {
                "Delhivery loads within the same city"

            }
            DemandType.Internal.type-> {
                "Delhivery loads between different cities"
            }
            DemandType.Others.type-> {
                "External client loads between different cities"
            }
            else -> "External client loads between different cities"
        }
       binding.textDescription = infoText
    }
}

/**
 * Loads filter view holder
 */
internal class HomeLoadsFilterItemVH(binding: ViewHomeLoadFilterTypesItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadFilterTypesItemBinding, HomeLoadsFilterItem>(binding) {
  override fun bind(
          item: HomeLoadsFilterItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
      binding.dlvIntracityToggle.text =  "${context.getString(R.string.action_dlv_intracity)} (${item.data.dlvIntracityCount})"
      binding.dlvIntercityToggle.text =   "${context.getString(R.string.action_dlv_intercity)} (${item.data.dlvIntercityCount})"
      binding.nonDlvToggle.text =   "${context.getString(R.string.action_non_delhivery)} (${item.data.nonDlvCount})"
      // filter visibility based on user's demand type
      binding.dlvIntercityToggle.visibility = if(item.data.userDemandType.contains(DemandType.Internal.type))View.VISIBLE else View.GONE
      binding.dlvIntracityToggle.visibility = if(item.data.userDemandType.contains(DemandType.Intracity.type))View.VISIBLE else View.GONE

      when (item.data.filterType) {
          DemandType.Intracity.type-> {
              binding.dlvIntracityToggle.isSelected = true
              binding.dlvIntercityToggle.isSelected = false
              binding.nonDlvToggle.isSelected = false
          }
          DemandType.Internal.type-> {
              binding.dlvIntracityToggle.isSelected = false
              binding.dlvIntercityToggle.isSelected = true
              binding.nonDlvToggle.isSelected = false
          }
          DemandType.Others.type-> {
              binding.dlvIntracityToggle.isSelected = false
              binding.dlvIntercityToggle.isSelected = false
              binding.nonDlvToggle.isSelected = true
          }
      }
      binding.dlvIntracityToggle.clickToAction(HomeLoadDlvIntracity, item, _interface)
      binding.dlvIntercityToggle.clickToAction(HomeLoadDlvIntercity, item, _interface)
      binding.nonDlvToggle.clickToAction(HomeLoadNonDlv, item, _interface)
  }
}


/**
 * Loads timeout view holder
 */
internal class HomeLoadsTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewTimeOutItemBinding, HomeLoadsTimeoutItem>(binding) {
  override fun bind(
          item: HomeLoadsTimeoutItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Info item view holder
 */
internal class HomeLoadsInfoItemVH(binding: ViewHomeLoadsInfoItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsInfoItemBinding, HomeLoadsInfoItem>(
            binding
    ) {
  override fun bind(
          item: HomeLoadsInfoItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
    val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.status_active))
    val searchString = SpannableString(item.data.searchString)
    searchString.setSpan(
            colorSpan, searchString.length - 12,
            searchString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    binding.textSearch.text = searchString
    binding.textSearch.clickToAction(HomeLoadsInfoAction_Search, item, _interface)
  }
}


/**
 * More Info item view holder
 */
internal class HomeLoadsMoreInfoItemVH(binding: ViewHomeLoadsMoreInfoItemBinding) :
  BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsMoreInfoItemBinding, HomeLoadsMoreInfoItem>(
          binding
  ) {
  override fun bind(
          item: HomeLoadsMoreInfoItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
    val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.status_active))

    val editRouteString: Spannable = SpannableString(item.data.editRouteString)
    editRouteString.setSpan(
            colorSpan, editRouteString.length - 6,
            editRouteString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    binding.textEditRoute.text = editRouteString
    binding.textEditRoute.clickToAction(HomeLoadsInfoAction_EditRoute, item, _interface)
  }

  internal class HomeLoadsSummaryItemVH(binding: ViewHomeSummaryItemBinding):
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeSummaryItemBinding, HomeLoadsSummaryItem>(
            binding
    ) {
    override fun bind(
            item: HomeLoadsSummaryItem,
            _interface: HomeLoadsRVAdapterInterface
    ) {
      if(_interface.fetchCurrSize()!=null && _interface.itemDeleted()){
        _interface.itemDeleted(false)
        binding.loadsCount.text = "(" + _interface.fetchCurrSize().toString() + ")"
      }else {
        _interface.updateCurrSize(item.data.count)
        binding.loadsCount.text = "(" + item.data.count.toString() + ")"
      }
    }
  }
}