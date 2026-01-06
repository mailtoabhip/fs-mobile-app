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
import com.delhivery.axle.data.home.loads.HomeLoadsVehicleFilterAction
import com.delhivery.axle.databinding.CardContractsIntercityTripsBidsBinding
import com.delhivery.axle.databinding.CardsContractsIntracityTripsBidsBinding
import com.delhivery.axle.databinding.ViewHomeContractsFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsIntracityFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsProgressItemBinding
import com.delhivery.axle.databinding.ViewSearchContractsItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.home.fragments.bids.adjustTextSizeToFit
import com.delhivery.axle.ui.home.fragments.bids.calculateAvailableCardWidth
import com.delhivery.axle.ui.home.fragments.bids.dpToPx


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
        
        // Determine drawable based on speed field
        val contractTypeDrawable = if (item.data.isExpress()) {
            Log.d("speed Delhivery", "Express: ${item.data.isExpress()}")
            R.drawable.ic_delhivery_bolt  // Express
        } else {
            Log.d("speed Delhivery", "Regular: ${item.data.isExpress()}")
            R.drawable.ic_truck_small  // Regular (NEXP or null)
        }
        
        // Set drawable start and colors
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(context, contractTypeDrawable), null, null, null
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

    // Apply dynamic text sizing AFTER visibility is set
    applyDynamicTextSizing(item)

    // Handle the included bottom button layout - set request data first (important for visibility)
    binding.reportingTimeButton.request = item.data
    
    // Make sure the container itself is visible and properly sized
    binding.reportingTimeButton.root.visibility = View.VISIBLE

    // Handle visibility based on the same logic as XML data binding:
    // android:text="@{request._requiredOn != null ? request.requiredAtWithTime() : request.getTimeOfContracts()}"
    val reportingTimeText = if (item.data._requiredOn != null) {
      item.data.requiredAtWithTime()
    } else {
      item.data.getTimeOfContracts()
    }
    
    // Show max width button only if there's no meaningful reporting time
    if (item.data._requiredOn == null && item.data.reportingTime == null) {
      // Hide reporting time section
      binding.reportingTimeButton.labelReporting.visibility = View.GONE
      binding.reportingTimeButton.reportingTime.visibility = View.GONE
      binding.reportingTimeButton.placeBidButton.visibility = View.GONE
      
      // Show full-width button
      binding.reportingTimeButton.placeBidButtonMaxWidth.visibility = View.VISIBLE
      binding.reportingTimeButton.placeBidButtonMaxWidth.text = "Place Bid"
      binding.reportingTimeButton.placeBidButtonMaxWidth.clickToAction(HomeBidsRequestAction_ViewDetails, item, _interface)
    } else {
      // Show reporting time section
      binding.reportingTimeButton.labelReporting.visibility = View.VISIBLE
      binding.reportingTimeButton.reportingTime.visibility = View.VISIBLE
      binding.reportingTimeButton.placeBidButton.visibility = View.VISIBLE
      binding.reportingTimeButton.reportingTime.text = reportingTimeText
      
      // Hide full-width button
      binding.reportingTimeButton.placeBidButtonMaxWidth.visibility = View.GONE
      binding.reportingTimeButton.placeBidButton.clickToAction(HomeBidsRequestAction_ViewDetails, item, _interface)
    }
  }

  /**
   * Apply dynamic text sizing for intercity contracts
   * Pure dynamic sizing approach - text scales down to fit in single line
   * Contracts don't show payment information
   */
  private fun applyDynamicTextSizing(item: HomeContractsRequestItem) {
    // Contracts only show truck info - no payment fields
    // Reset to default text size as it's a single view
    binding.truckInfo?.let { resetTextViewToDefault(it) }
  }
  
  /**
   * Reset TextView to default size
   */
  private fun resetTextViewToDefault(textView: android.widget.TextView) {
    textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
  }

   fun stopCounter() {
    countDownTimer?.cancel()
    countDownTimer = null
  }

}

/**
 * Intracity Contract request item view holder
 */
class HomeContractsIntracityRequestItemVH(binding: CardsContractsIntracityTripsBidsBinding) :
  BaseHomeContractsRVAdapterViewHolder<CardsContractsIntracityTripsBidsBinding, HomeContractsRequestItem>(
    binding
  ) {

  override fun bind(
    item: HomeContractsRequestItem,
    _interface: HomeContractsRVAdapterInterface
  ) {

      Log.d("test", "test")
    // Set the request data for data binding
    binding.request = item.data
    binding.executePendingBindings()
    
    // Determine contract type
    val demandType = item.data.getDemandTypeByLoad()
    
    // Set contract type display (Delhivery Contract or Client Contract)
    if (demandType == "Delhivery Load") {
        binding.demandLoadType.text = "Delhivery Contract"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.delhivery_load_bg)
        
        // Determine drawable based on speed field
        val contractTypeDrawable = if (item.data.isExpress()) {
            Log.d("speed Delhivery", "Express: ${item.data.isExpress()}")
            R.drawable.ic_delhivery_bolt  // Express
        } else {
            Log.d("speed Delhivery", "Regular: ${item.data.isExpress()}")
            R.drawable.ic_truck_small  // Regular (NEXP or null)
        }
        
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(context, contractTypeDrawable), null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.delhivery_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.delhivery_load_color))
    } else {
        binding.demandLoadType.text = "Client Contract"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.client_load_bg)
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(context, R.drawable.ic_user), null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.client_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.client_load_color))
    }

    // Apply dynamic text sizing - contracts don't show payment info
    applyIntracityDynamicTextSizing(item)

    // Handle the included bottom button layout - set request data first (important for visibility)
    binding.containerError.request = item.data
    
    // Make sure the container itself is visible and properly sized
    binding.containerError.root.visibility = View.VISIBLE

    // Handle visibility based on the same logic as XML data binding:
    // android:text="@{request._requiredOn != null ? request.requiredAtWithTime() : request.getTimeOfContracts()}"
    val reportingTimeText = if (item.data._requiredOn != null) {
      item.data.requiredAtWithTime()
    } else {
      item.data.getTimeOfContracts()
    }
    
    // Show max width button only if there's no meaningful reporting time
    if (item.data._requiredOn == null && item.data.reportingTime == null) {
      // Hide reporting time section
      binding.containerError.labelReporting.visibility = View.GONE
      binding.containerError.reportingTime.visibility = View.GONE
      binding.containerError.placeBidButton.visibility = View.GONE
      
      // Show full-width button
      binding.containerError.placeBidButtonMaxWidth.visibility = View.VISIBLE
      binding.containerError.placeBidButtonMaxWidth.text = "Place Bid"
      binding.containerError.placeBidButtonMaxWidth.clickToAction(HomeBidsRequestAction_ViewDetails, item, _interface)
    } else {
      // Show reporting time section
      binding.containerError.labelReporting.visibility = View.VISIBLE
      binding.containerError.reportingTime.visibility = View.VISIBLE
      binding.containerError.placeBidButton.visibility = View.VISIBLE
      binding.containerError.reportingTime.text = reportingTimeText
      
      // Hide full-width button
      binding.containerError.placeBidButtonMaxWidth.visibility = View.GONE
      binding.containerError.placeBidButton.clickToAction(HomeBidsRequestAction_ViewDetails, item, _interface)
    }
  }

  /**
   * Apply dynamic text sizing for intracity contracts
   * Pure dynamic sizing approach - text scales down to fit in single line
   * Contracts don't show payment information
   */
  private fun applyIntracityDynamicTextSizing(item: HomeContractsRequestItem) {
    // Contracts only show truck info - no payment fields
    // Reset to default text size as it's a single view
    binding.truckInfo?.let { resetTextViewToDefault(it) }
  }
  
  /**
   * Reset TextView to default size
   */
  private fun resetTextViewToDefault(textView: android.widget.TextView) {
    textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
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
      binding.filterIcon.clickToAction(HomeLoadsVehicleFilterAction,item,_interface)
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
     "Corporate", "Internal", "Internal,Corporate" -> {
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

