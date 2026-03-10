package com.delhivery.axle.ui.home.fragments.loads

import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_AcceptBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_NavigationMap
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.SUB_REQUEST_TYPE_INTRACITY
import com.delhivery.axle.data.home.loads.*
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.home.fragments.bids.calculateAvailableCardWidth
import com.delhivery.axle.ui.home.fragments.bids.dpToPx
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
class HomeLoadsRequestItemVH(binding: LoadDelhiveryIntercityV2Binding) :
    BaseHomeLoadsRVAdapterViewHolder<LoadDelhiveryIntercityV2Binding, HomeLoadsRequestItem>(
            binding
    ) {

  override fun bind(
          item: HomeLoadsRequestItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
      Log.d("Reprting time debug", "${absoluteAdapterPosition}, ${item.data._requiredOn}, ${item.data.reportingTime}")
      val demandType = item.data.getDemandTypeByLoad()
      Log.d("adapterData", "${item.data}")
      Log.d("HomeLoadsRVAdapter", "Speed field: ${item.data.speed}, isExpress: ${item.data.isExpress()}, DemandType: $demandType")

    if(item.data.subRequestType == SUB_REQUEST_TYPE_INTRACITY){
        // Apply dynamic width constraints FIRST
        applyIntracityDynamicConstraints()
        
        binding.layoutIntracity.request = item.data
        binding.layoutIntracity.containerError.request = item.data
        binding.layoutIntracity.root.visibility = View.VISIBLE
        binding.cvIntercity.visibility = View.GONE
        
        // Set load type display for intracity
        if (demandType == "Delhivery Load") {
            binding.layoutIntracity.demandLoadType.text = "Delhivery Load"
            binding.layoutIntracity.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.delhivery_load_bg)
            
            // Determine drawable based on speed field
            val loadTypeDrawable = if (item.data.isExpress()) {
                R.drawable.ic_delhivery_bolt  // Express
            } else {
                R.drawable.ic_truck_small  // Regular (NEXP or null)
            }
            
            binding.layoutIntracity.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, loadTypeDrawable), null, null, null
            )
            binding.layoutIntracity.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.delhivery_load_color))
            binding.layoutIntracity.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.delhivery_load_color))
            } else if (demandType == "Facility Arranged") {
            binding.layoutIntracity.demandLoadType.text = "Facility Arranged"
            binding.layoutIntracity.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.facility_load_bg)
            
            // Clear any previous drawable for Facility Arranged
            binding.layoutIntracity.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
                null, null, null, null
            )
            binding.layoutIntracity.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.facility_load_color))
        } else if (demandType == "Client Load") {
            binding.layoutIntracity.demandLoadType.text = "Client Load"
            binding.layoutIntracity.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.client_load_bg)
            
            // Determine drawable based on speed field - show bolt icon for Express loads
            val loadTypeDrawable = if (item.data.isExpress()) {
                R.drawable.ic_delhivery_bolt  // Express
            } else {
                R.drawable.ic_user  // Regular (NEXP or null)
            }
            
            binding.layoutIntracity.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, loadTypeDrawable), null, null, null
            )
            binding.layoutIntracity.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.client_load_color))
            binding.layoutIntracity.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.client_load_color))
        }
        
        // Set payment mode display for intracity loads - only show for valid payment modes
        if (item.data.shouldShowPaymentMode()) {
            binding.layoutIntracity.paymentType.visibility = View.VISIBLE
            binding.layoutIntracity.paymentType.text = item.data.getPaymentModeDisplay()
            // Only show advance percentage for advance payment mode
            if (item.data.shouldShowAdvancePercentage()) {
                binding.layoutIntracity.advancePaymentPercentage.visibility = View.VISIBLE
                binding.layoutIntracity.advancePaymentPercentage.text = item.data.getAdvancePaymentPercentage()
            } else {
                binding.layoutIntracity.advancePaymentPercentage.visibility = View.GONE
            }
        } else {
            binding.layoutIntracity.paymentType.visibility = View.GONE
            binding.layoutIntracity.advancePaymentPercentage.visibility = View.GONE
        }
        
//        binding.layoutIntracity.containerRowBidFor1.placeBidButton1.rootView.
//        binding.nonIntracityLayout.visibility = View.GONE
//        binding.layoutIntracity.layoutTransaction.navigate.visibility = View.VISIBLE
//        binding.layoutIntracity.containerRowBidFor1.placeBidButton1.clickToAction(HomeBidsRequestAction_NavigationMap, item, bindingAdapterPosition, _interface)
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
            binding.layoutIntracity.containerError.labelReporting.visibility = View.GONE
            binding.layoutIntracity.containerError.reportingTime.visibility = View.GONE
            binding.layoutIntracity.containerError.placeBidButton.visibility = View.GONE
            
            // Show full-width button
            binding.layoutIntracity.containerError.placeBidButtonMaxWidth.visibility = View.VISIBLE
            binding.layoutIntracity.containerError.placeBidButtonMaxWidth.text = "Accept For ${item.data.formatedTargetPrice()}"
            binding.layoutIntracity.containerError.placeBidButtonMaxWidth.clickToAction(HomeBidsRequestAction_AcceptBid, item, bindingAdapterPosition, _interface)
            
            // Force updates
            binding.layoutIntracity.containerError.executePendingBindings()
        } else {
            // Show reporting time section
            binding.layoutIntracity.containerError.labelReporting.visibility = View.VISIBLE
            binding.layoutIntracity.containerError.reportingTime.visibility = View.VISIBLE
            binding.layoutIntracity.containerError.placeBidButton.visibility = View.VISIBLE
            binding.layoutIntracity.containerError.reportingTime.text = reportingTimeText
            
            // Hide full-width button
            binding.layoutIntracity.containerError.placeBidButtonMaxWidth.visibility = View.GONE
            binding.layoutIntracity.containerError.placeBidButton.clickToAction(HomeBidsRequestAction_AcceptBid, item, bindingAdapterPosition, _interface)
        }
        
        // Force data binding updates to be applied immediately
        binding.layoutIntracity.containerError.executePendingBindings()
        binding.layoutIntracity.executePendingBindings()
        
//        includedBinding.placeBidButton1.rootView.setOnClickListener {
//            Log.d("Intracity Card Clicked1", "rjrfv")
//            it.clickToAction(HomeBidsRequestAction_AcceptBid, item, bindingAdapterPosition, _interface)
//        }

//        includedBinding.btnAcceptBid.setOnClickListener {
//            Log.d("Intracity Card Clicked2", "rjrfv")
//            it.clickToAction(HomeBidsRequestAction_AcceptBid, item, bindingAdapterPosition, _interface)
//        }

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
        binding.containerError.request = item.data
        
        // Set payment mode display - only show for valid payment modes
        if (item.data.shouldShowPaymentMode()) {
            binding.paymentType.visibility = View.VISIBLE
            binding.paymentType.text = item.data.getPaymentModeDisplay()
            // Only show advance percentage for advance payment mode
            if (item.data.shouldShowAdvancePercentage()) {
                binding.advancePaymentPercentage.visibility = View.VISIBLE
                binding.advancePaymentPercentage.text = item.data.getAdvancePaymentPercentage()
            } else {
                binding.advancePaymentPercentage.visibility = View.GONE
            }
        } else {
            binding.paymentType.visibility = View.GONE
            binding.advancePaymentPercentage.visibility = View.GONE
        }
        
        // Apply dynamic text sizing AFTER visibility is set
        applyIntercityDynamicTextSizing()
        
        if (demandType == "Delhivery Load") {
            binding.demandLoadType.text = "Delhivery Load"
            binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.delhivery_load_bg)
            
            // Determine drawable based on speed field
            val loadTypeDrawable = if (item.data.isExpress()) {
                R.drawable.ic_delhivery_bolt  // Express
            } else {
                R.drawable.ic_truck_small  // Regular (NEXP or null)
            }
            
            binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, loadTypeDrawable), null, null, null
            )
            binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.delhivery_load_color))
            binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.delhivery_load_color))
        } else if (demandType == "Facility Arranged") {
            binding.demandLoadType.text = "Facility Arranged"
            binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.facility_load_bg)
            
            // Clear any previous drawable for Facility Arranged
            binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
                null, null, null, null
            )
            binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.facility_load_color))
        } else if (demandType == "Client Load") {
            binding.demandLoadType.text = "Client Load"
            binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.client_load_bg)
            
            // Determine drawable based on speed field - show bolt icon for Express loads
            val loadTypeDrawable = if (item.data.isExpress()) {
                R.drawable.ic_delhivery_bolt  // Express
            } else {
                R.drawable.ic_user  // Regular (NEXP or null)
            }
            
            binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, loadTypeDrawable), null, null, null
            )
            binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.client_load_color))
            binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.client_load_color))
        }
        if(item.data.isDMTIndent()){
            binding.closingTime.visibility = View.GONE
        }else{
            if(item.data.bidEndingTime.isNotNullOrEmpty() && item.data.transactionBid== null){
                binding.closingTime.visibility = View.VISIBLE
                }else{
                    binding.closingTime.visibility = View.GONE
                }

        }

        if(item.data.indentOrigin.equals("LH")){
            // Removed Stops View
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

        }

        // Handle visibility based on the same logic as XML data binding:
        // android:text="@{request._requiredOn != null ? request.requiredAtWithTime() : request.getTimeOfContracts()}"
        val reportingTimeIntercity = if (item.data._requiredOn != null) {
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
            binding.containerError.placeBidButtonMaxWidth.clickToAction(HomeBidsRequestAction_PlaceBid, item, bindingAdapterPosition, _interface)
        } else {
            // Show reporting time section
            binding.containerError.labelReporting.visibility = View.VISIBLE
            binding.containerError.reportingTime.visibility = View.VISIBLE
            binding.containerError.placeBidButton.visibility = View.VISIBLE
            binding.containerError.reportingTime.text = reportingTimeIntercity
            
            // Hide full-width button
            binding.containerError.placeBidButtonMaxWidth.visibility = View.GONE
            binding.containerError.placeBidButton.clickToAction(HomeBidsRequestAction_PlaceBid, item, bindingAdapterPosition, _interface)
        }
        
        // Force data binding updates to be applied immediately
        binding.executePendingBindings()
    }

//    binding.viewBidInfo.clickToAction(HomeBidsRequestAction_PlaceBid, item, bindingAdapterPosition, _interface
//    )
  }

  /**
   * Apply dynamic text sizing for intercity layout
   * Adjusts text size based on actual content length to prevent truncation
   */
  private fun applyIntercityDynamicTextSizing() {
    val availableWidth = context.calculateAvailableCardWidth(
      cardMarginDp = 12,
      contentPaddingDp = 16
    )
    
    val spacing8dpPx = 8.dpToPx(context)
    val spacing2dpPx = 2.dpToPx(context)
    val drawablePaddingPx = 4.dpToPx(context)
    
    val shouldShowPaymentType = binding.paymentType?.visibility == View.VISIBLE
    val shouldShowAdvancePercentage = binding.advancePaymentPercentage?.visibility == View.VISIBLE
    
//    binding.truckInfo?.let { truckInfo ->
//      when {
//        // All 3 views visible: truckInfo (8dp) paymentType (2dp) percentage
//        shouldShowAdvancePercentage -> {
//          binding.paymentType?.let { paymentType ->
//            binding.advancePaymentPercentage?.let { percentage ->
//              adjustTextSizeToFit(
//                views = listOf(truckInfo, paymentType, percentage),
//                textSizes = listOf(14f, 14f, 14f),
//                availableWidth = availableWidth,
//                minTextSize = 10f,
//                spacingBetweenViews = listOf(spacing8dpPx, spacing2dpPx),
//                drawablePadding = drawablePaddingPx
//              )
//            }
//          }
//        }
//        // Only truck + payment type: uniform 8dp spacing
//        shouldShowPaymentType -> {
//          binding.paymentType?.let { paymentType ->
//            adjustTextSizeToFit(
//              views = listOf(truckInfo, paymentType),
//              textSizes = listOf(14f, 14f),
//              availableWidth = availableWidth,
//              minTextSize = 10f,
//              spacingBetweenViews = spacing8dpPx,
//              drawablePadding = drawablePaddingPx
//            )
//          }
//        }
//        // Only truck info - reset to default
//        else -> {
//          resetTextViewToDefault(truckInfo, 14f)
//        }
//      }
//    }
  }

  /**
   * Apply dynamic text sizing for intracity layout
   * Adjusts text size based on actual content length to prevent truncation
   */
  private fun applyIntracityDynamicConstraints() {
    // Calculate available width
    val availableWidth = context.calculateAvailableCardWidth(
      cardMarginDp = 0,  // No card margins in this layout
      contentPaddingDp = 16  // Content padding
    )
    
    val spacing8dpPx = 8.dpToPx(context)
    val spacing2dpPx = 2.dpToPx(context)
    val drawablePaddingPx = 4.dpToPx(context)
    
    // Apply dynamic text sizing to city row (fromCity and pincodeState)
    applyIntracityToCityRow(availableWidth)
    
    // Apply dynamic text sizing to truck info + payment row
    val shouldShowPaymentType = binding.layoutIntracity.paymentType?.visibility == View.VISIBLE
    val shouldShowAdvancePercentage = binding.layoutIntracity.advancePaymentPercentage?.visibility == View.VISIBLE
    
//    binding.layoutIntracity.truckInfo?.let { truckInfo ->
//      when {
//        // All 3 views visible: truckInfo (8dp) paymentType (2dp) percentage
//        shouldShowAdvancePercentage -> {
//          binding.layoutIntracity.paymentType?.let { paymentType ->
//            binding.layoutIntracity.advancePaymentPercentage?.let { percentage ->
//              adjustTextSizeToFit(
//                views = listOf(truckInfo, paymentType, percentage),
//                textSizes = listOf(14f, 14f, 14f),
//                availableWidth = availableWidth,
//                minTextSize = 10f,
//                spacingBetweenViews = listOf(spacing8dpPx, spacing2dpPx),
//                drawablePadding = drawablePaddingPx
//              )
//            }
//          }
//        }
//        // Only truck + payment type: uniform 8dp spacing
//        shouldShowPaymentType -> {
//          binding.layoutIntracity.paymentType?.let { paymentType ->
//            adjustTextSizeToFit(
//              views = listOf(truckInfo, paymentType),
//              textSizes = listOf(14f, 14f),
//              availableWidth = availableWidth,
//              minTextSize = 10f,
//              spacingBetweenViews = spacing8dpPx,
//              drawablePadding = drawablePaddingPx
//            )
//          }
//        }
//        // Only truck info - reset to default
//        else -> {
//          resetTextViewToDefault(truckInfo, 14f)
//        }
//      }
//    }
  }
  
  /**
   * Apply dynamic text sizing to fromCity and pincode_state views for intracity loads
   * Uses adjustTextSizeToFit method to maintain relative size difference (16sp vs 14sp)
   * Prevents text truncation on smaller screens
   */
  private fun applyIntracityToCityRow(availableWidth: Int) {
    val spacingPx = 4.dpToPx(context) // marginStart="@dimen/size_4dp" from XML
    val drawablePaddingPx = 0 // No drawables in city row

    val fromCity = binding.layoutIntracity.fromCity
    val pincodeState = binding.layoutIntracity.pincodeState

//    if (fromCity != null && pincodeState != null && availableWidth > 0) {
//      // Use adjustTextSizeToFit with different text sizes to maintain hierarchy
//      // 16sp for fromCity, 14sp for pincodeState
//      adjustTextSizeToFit(
//        views = listOf(fromCity, pincodeState),
//        textSizes = listOf(16f, 14f),
//        availableWidth = availableWidth,
//        minTextSize = 10f,
//        spacingBetweenViews = spacingPx,
//        drawablePadding = drawablePaddingPx
//      )
//    }
  }
  
  /**
   * Reset TextView to default size
   */
  private fun resetTextViewToDefault(textView: TextView, defaultSize: Float = 14f) {
    textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, defaultSize)
  }

}

/**
 * Marketplace load item view holder
 */
class HomeLoadsMarketplaceItemVH(binding: CardLoadsDelhiveryMarketplaceBinding) :
    BaseHomeLoadsRVAdapterViewHolder<CardLoadsDelhiveryMarketplaceBinding, HomeLoadsMarketplaceItem>(
        binding
    ) {

  override fun bind(
      item: HomeLoadsMarketplaceItem,
      _interface: HomeLoadsRVAdapterInterface
  ) {
    Log.d("MarketplaceCard", "Binding marketplace load: ${item.data.transactionId}")
    Log.d("MarketplaceCard", "Shipper price: ${item.data.shipperPrice}")
    Log.d("MarketplaceCard", "Formatted shipper price: ${item.data.getFormattedShipperPrice()}")
    
    // Set basic data binding
    binding.request = item.data
    binding.containerError.request = item.data
    
    // Handle closing time visibility - Use contractBiddingEndTime for marketplace loads
    if(item.data.contractBiddingEndTime.isNullOrEmpty() || item.data.contractBiddingEndTime.equals("null", ignoreCase = true)){
      binding.closingTime.visibility = View.GONE
    } else {
      binding.closingTime.visibility = View.VISIBLE
      binding.closingTime.text = item.data.formattedContractBiddingEndTime()
    }
    
    // Handle payment mode display - MARKETPLACE SPECIFIC LOGIC
    if (item.data.shouldShowMarketplacePaymentMode()) {
      binding.paymentType.visibility = View.VISIBLE
      binding.paymentType.text = item.data.getMarketplacePaymentModeDisplay()
      // Only show advance percentage for advance payment mode
      if (item.data.shouldShowMarketplaceAdvancePercentage()) {
        binding.advancePaymentPercentage.visibility = View.VISIBLE
        binding.advancePaymentPercentage.text = item.data.getMarketplaceAdvancePaymentPercentage()
      } else {
        binding.advancePaymentPercentage.visibility = View.GONE
      }
    } else {
      binding.paymentType.visibility = View.GONE
      binding.advancePaymentPercentage.visibility = View.GONE
    }
    
    // Handle supplier name - hide if empty
    if (!item.data.shipperName.isNullOrEmpty()) {
      binding.supplierName.visibility = View.VISIBLE
    } else {
      binding.supplierName.visibility = View.INVISIBLE
    }
    
    // Handle supplier phone number - use as-is from API (already masked)
    if (!item.data.shipperPhoneNumber.isNullOrEmpty()) {
      binding.supplierNumber.visibility = View.VISIBLE
    } else {
      binding.supplierNumber.visibility = View.GONE
    }
      // MARKETPLACE SPECIFIC: Always show shipper price and place bid button
    binding.containerError.labelReporting.visibility = View.VISIBLE
    binding.containerError.placeBidButton.visibility = View.VISIBLE
    binding.containerError.placeBidButtonMaxWidth.visibility = View.GONE
    binding.containerError.placeBidButton.clickToAction(HomeBidsRequestAction_PlaceBid, item, bindingAdapterPosition, _interface)
    
    // Force data binding updates to be applied immediately
    binding.containerError.executePendingBindings()
    binding.executePendingBindings()
    
    // Apply dynamic width constraints AFTER visibility is set
    applyMarketplaceDynamicConstraints(item.data)
  }

  /**
   * Apply dynamic text sizing for marketplace layout
   * Adjusts text size based on actual content length to prevent truncation
   */
  private fun applyMarketplaceDynamicConstraints(data: HomeBidsRequestItemData) {
    val availableWidth = context.calculateAvailableCardWidth(
      cardMarginDp = 12,
      contentPaddingDp = 16
    )
    
    val spacing8dpPx = 8.dpToPx(context)
    val spacing2dpPx = 2.dpToPx(context)
    val drawablePaddingPx = 4.dpToPx(context)
    
    // Determine visibility based on data
    val shouldShowPaymentType = data.shouldShowMarketplacePaymentMode()
    val shouldShowAdvancePercentage = data.shouldShowMarketplaceAdvancePercentage()
    
//    binding.truckInfo?.let { truckInfo ->
//      when {
//        // All 3 views visible: truckInfo (8dp) paymentType (2dp) percentage
//        shouldShowAdvancePercentage -> {
//          binding.paymentType?.let { paymentType ->
//            binding.advancePaymentPercentage?.let { percentage ->
//              adjustTextSizeToFit(
//                views = listOf(truckInfo, paymentType, percentage),
//                textSizes = listOf(14f, 14f, 14f),
//                availableWidth = availableWidth,
//                minTextSize = 10f,
//                spacingBetweenViews = listOf(spacing8dpPx, spacing2dpPx),
//                drawablePadding = drawablePaddingPx
//              )
//            }
//          }
//        }
//        // Only truck + payment type: uniform 8dp spacing
//        shouldShowPaymentType -> {
//          binding.paymentType?.let { paymentType ->
//            adjustTextSizeToFit(
//              views = listOf(truckInfo, paymentType),
//              textSizes = listOf(14f, 14f),
//              availableWidth = availableWidth,
//              minTextSize = 10f,
//              spacingBetweenViews = spacing8dpPx,
//              drawablePadding = drawablePaddingPx
//            )
//          }
//        }
//        // Only truck info - reset to default
//        else -> {
//          resetTextViewToDefault(truckInfo, 14f)
//        }
//      }
//    }
  }
  
  /**
   * Reset TextView to default size
   */
  private fun resetTextViewToDefault(textView: TextView, defaultSize: Float = 14f) {
    textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, defaultSize)
  }
}


/**
 * Kyc pending view holder
 */
internal class HomeLoadsKycPendingItemVH(binding: CardKycPendingBannerBinding) :
    BaseHomeLoadsRVAdapterViewHolder<CardKycPendingBannerBinding, HomeLoadsKycPendingItem>(
        binding
    ) {
    override fun bind(
        item: HomeLoadsKycPendingItem,
        _interface: HomeLoadsRVAdapterInterface
    ) {
        // Set click action for the entire card
        binding.alertContainer.clickToAction(
            HomeLoadsKycPendingAction,
            item,
            _interface
        )
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
internal class HomeLoadsSearchItemVH(binding: ViewHomeLoadsSearchPlaceholderItemV2Binding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsSearchPlaceholderItemV2Binding, HomeLoadsSearchItem>(
            binding
    ) {
  override fun bind(
          item: HomeLoadsSearchItem,
          _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.editStickySearch.clickToAction(HomeLoadsSearchAction_Search,item,_interface)
    binding.filterIcon.clickToAction(HomeLoadsVehicleFilterAction,item,_interface)
    //binding.spinnerTruckDisplayName.text = "Vehicle Type"+if(item.data.query?.split(",")?.size==0)"" else " : " + item.data.query?.split(",")?.joinToString(", ")
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

/**
 *   Add Marketplace info
 */
internal class HomeLoadMarketPlaceInfoItemVH(binding: ViewHomeMarketplaceInfoBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeMarketplaceInfoBinding, HomeMarketPlaceInfoItem>(
        binding
    ) {
    override fun bind(
        item: HomeMarketPlaceInfoItem,
        _interface: HomeLoadsRVAdapterInterface
    ) {
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
      binding.dlvIntracityToggle.text =  "${context.getString(R.string.intracity)} (${item.data.dlvIntracityCount})"
      binding.dlvIntercityToggle.text =   "${context.getString(R.string.intercity)} (${item.data.dlvIntercityCount + item.data.nonDlvCount})"
      binding.dlvMarketplaceToggle.text =   "${context.getString(R.string.market_place)} (${item.data.marketplaceCount})"
      //binding.nonDlvToggle.text =   "${context.getString(R.string.action_non_delhivery)} (${item.data.nonDlvCount})"
      // filter visibility based on user's demand type
      binding.dlvIntercityToggle.visibility = if(item.data.userDemandType.contains(DemandType.Internal.type) || item.data.userDemandType.contains(DemandType.Others.type))View.VISIBLE else View.GONE
      binding.dlvIntracityToggle.visibility = if(item.data.userDemandType.contains(DemandType.Intracity.type)|| item.data.userDemandType.contains(DemandType.Intracity_OPS.type))View.VISIBLE else View.GONE
      binding.dlvMarketplaceToggle.visibility = if(item.data.userDemandType.contains(DemandType.Spot_Marketplace.type))View.VISIBLE else View.GONE

      //Comment below line once implementation is complete
      //binding.dlvMarketplaceToggle.visibility = View.VISIBLE

      when (item.data.filterType) {
          DemandType.Intracity.type-> {
              binding.dlvIntracityToggle.isSelected = true
              binding.dlvIntercityToggle.isSelected = false
              binding.dlvMarketplaceToggle.isSelected = false
              //binding.nonDlvToggle.isSelected = false
          }
          DemandType.Internal.type, DemandType.Others.type-> {
              binding.dlvIntracityToggle.isSelected = false
              binding.dlvIntercityToggle.isSelected = true
              binding.dlvMarketplaceToggle.isSelected = false
              //binding.nonDlvToggle.isSelected = false
          }
          "Marketplace"-> {
              binding.dlvIntracityToggle.isSelected = false
              binding.dlvIntercityToggle.isSelected = false
              binding.dlvMarketplaceToggle.isSelected = true
          }
      }
      binding.dlvIntracityToggle.clickToAction(HomeLoadDlvIntracity, item, _interface)
      binding.dlvIntercityToggle.clickToAction(HomeLoadDlvIntercity, item, _interface)
      binding.dlvMarketplaceToggle.clickToAction(HomeLoadMarketplace, item, _interface)
      //binding.nonDlvToggle.clickToAction(HomeLoadNonDlv, item, _interface)
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