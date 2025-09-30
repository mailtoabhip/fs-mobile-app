package com.delhivery.axle.ui.home.fragments.contracts

import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.contracts.HomeContractsFilterExpress
import com.delhivery.axle.data.home.contracts.HomeContractsFilterIntracity
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterAll
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterFixed
import com.delhivery.axle.data.home.contracts.HomeContractsIntracityFilterFlexible
import com.delhivery.axle.databinding.CardContractsIntercityTripsBidsBinding
import com.delhivery.axle.databinding.ViewHomeContractsFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsIntracityFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsProgressItemBinding
import com.delhivery.axle.databinding.ViewSearchContractsItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder


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
class HomeContractsRequestItemVH(binding: CardContractsIntercityTripsBidsBinding) :
  BaseHomeContractsRVAdapterViewHolder<CardContractsIntercityTripsBidsBinding, HomeContractsRequestItem>(
    binding
  ) {

  override fun bind(
    item: HomeContractsRequestItem,
    _interface: HomeContractsRVAdapterInterface
  ) {
    // Set the request data for data binding - this will automatically bind most elements in XML
    binding.request = item.data

    // Determine contract type (same logic as loads but for contracts)
    val demandType = item.data.getDemandTypeByLoad()
    
    // Set contract type display (similar to loads logic)
    if (demandType == "Delhivery Load") {
        binding.demandLoadType.text = "Delhivery Contract"
        // Use background tint to preserve rounded corners
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.delhivery_load_bg)
        // Set drawable start and colors
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(context, R.drawable.ic_truck_small), null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.delhivery_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.delhivery_load_color))
    } else if (demandType == "Client Load") {
        binding.demandLoadType.text = "Client Contract"
        // Use background tint to preserve rounded corners
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.client_load_bg)
        // Set drawable start and colors
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(context, R.drawable.ic_user), null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.client_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.client_load_color))
    }

    // Handle the included bottom button layout - set request data first (important for visibility)
    binding.containerError.request = item.data
    
    // Make sure the container itself is visible and properly sized
    binding.containerError.root.visibility = View.VISIBLE

    if(item.data.reportingTime!=null) {
      binding.containerError.clPlaceBidReportTime.visibility = View.VISIBLE
      binding.containerError.placeBidButton.visibility = View.VISIBLE
      // Programmatically set reporting time text (overrides data binding for contracts)
      binding.containerError.reportingTime.text = item.data.getTimeOfContracts()
      binding.containerError.placeBidButton.clickToAction(HomeBidsRequestAction_ViewDetails, item, _interface)
      Log.d("ContainerError", "Showing reporting time layout: ${item.data.getTimeOfContracts()}")
    }
    
    // Log for debugging
    Log.d("contractTag"," required On ${item.data._requiredOn} reqTime ${item.data.requiredAtWithTime()} reportTime ${item.data.reportingTime}")
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
    //binding.info.clickToAction(HomeContractsFilterInfo,item,_interface)

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
     val combinedExpressCount = item.data.expressCount + item.data.nonExpressCount
     binding.expressToggle.text =   "${context.getString(string.intercity)} ($combinedExpressCount)"
     binding.intracityToggle.text =   "${context.getString(string.intracity)} (${item.data.intraCity})"

        binding.expressToggle.visibility = if((item.data.userDemandType.contains(DemandType.Internal.type)&&item.data.userContractDemand)||item.data.userDemandType.contains(DemandType.Others.type))View.VISIBLE else View.GONE
        binding.intracityToggle.visibility = if(item.data.userDemandType.contains(DemandType.Intracity.type)&&item.data.userContractDemand)View.VISIBLE else View.GONE

    when (item.data.filterType) {
     "Corporate", "Internal"-> {
        // Both Corporate (non-express) and Internal (express) now use the express toggle
        binding.expressToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        binding.intracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.expressToggle.isSelected = true
        binding.intracityToggle.isSelected=false
      }
      "Intracity" -> {
        binding.expressToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
        binding.intracityToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        binding.expressToggle.isSelected = false
        binding.intracityToggle.isSelected=true
      }
    }

    binding.expressToggle.clickToAction(HomeContractsFilterExpress, item, _interface)
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

