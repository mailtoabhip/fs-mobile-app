package com.delhivery.axle.ui.home.fragments.bids

import android.util.Log
import androidx.databinding.ViewDataBinding
import android.view.View
import android.widget.TextView
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.home.bids.*
import com.delhivery.axle.databinding.CardBidsDelhiveryMarketplaceBinding
import com.delhivery.axle.databinding.CardCommonBidsV2Binding
import com.delhivery.axle.databinding.CardCommonIntracityBidsBinding
import com.delhivery.axle.databinding.ViewBidsHeaderNewItemBinding
import com.delhivery.axle.databinding.ViewBidsSearchbarNewItemBinding
import com.delhivery.axle.databinding.ViewContractsBidItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.bids.BidType
import android.content.Context

/**
 * Dynamically adjusts text size of multiple TextViews to fit within available width
 * Measures actual text content and scales down if needed so nothing gets truncated
 */
fun adjustTextSizeToFit(
  views: List<TextView>,
  availableWidth: Int,
  defaultTextSize: Float,
  minTextSize: Float,
  spacingBetweenViews: Int,
  drawablePadding: Int
) {
  if (views.isEmpty()) return
  
  // First, set all views to default text size
  views.forEach { it.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, defaultTextSize) }
  
  // Measure total width needed with default text size
  var totalWidthNeeded = 0f
  
  views.forEachIndexed { index, textView ->
    // Measure text width
    val text = textView.text?.toString() ?: ""
    val textWidth = textView.paint.measureText(text)
    
    // Account for drawable (icon) width if present
    val drawableWidth = textView.compoundDrawables[0]?.intrinsicWidth ?: 0
    val drawableSpace = if (drawableWidth > 0) drawableWidth + drawablePadding else 0
    
    // Account for padding
    val paddingSpace = textView.paddingStart + textView.paddingEnd
    
    totalWidthNeeded += textWidth + drawableSpace + paddingSpace
    
    // Add spacing between views (except for last view)
    if (index < views.size - 1) {
      totalWidthNeeded += spacingBetweenViews
    }
  }
  
  // If total width exceeds available width, scale down text size proportionally
  if (totalWidthNeeded > availableWidth) {
    val scaleFactor = availableWidth / totalWidthNeeded
    val newTextSize = (defaultTextSize * scaleFactor).coerceAtLeast(minTextSize)
    
    // Apply the scaled text size to all views
    views.forEach { textView ->
      textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, newTextSize)
    }
  }
  
  // Request layout update
  views.forEach { it.requestLayout() }
}

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseHomeBidsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeBidsRVAdapterItem<*>>(binding: B) :
  BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean = false
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  protected fun View.clickToActionWithStateSelection(
    actionId: String,
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  ) {
    setOnClickListener {
      if(this.isSelected) return@setOnClickListener
      action(actionId, item, _interface)
    }
  }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Header item view holder
 */
internal class HomeBidsHeaderItemVH(binding: ViewBidsHeaderNewItemBinding) :
  BaseHomeBidsRVAdapterViewHolder<ViewBidsHeaderNewItemBinding, HomeBidsHeaderItem>(binding) {
  override fun bind(
    item: HomeBidsHeaderItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {

    binding.ongoingCount = item.data.myBids
    if (isLoadingData) {
      // Disable tab clicks when loading
      binding.tvOngoing.setOnClickListener(null)
      binding.tvOngoing.isEnabled = false
      //binding.tvOngoing.alpha = 0.5f
      // Add loading indicator text
      binding.tvOngoing.text = "Ongoing (${item.data.myBids})..."
    } else {
      // Enable tab clicks when not loading
      binding.tvOngoing.clickToActionWithStateSelection(HomeBidsHeaderAction_TabChangeActive, item, _interface)
      binding.tvOngoing.isEnabled = true
      binding.tvOngoing.alpha = 1.0f
      // Remove loading indicator text
      binding.tvOngoing.text = "Ongoing (${item.data.myBids})"
    }

    //
    binding.closedCount = item.data.confirmedBid + item.data.lostBids
    if (isLoadingData) {
      // Disable tab clicks when loading
      binding.tvClosed.setOnClickListener(null)
      binding.tvClosed.isEnabled = false
      //binding.tvWon.alpha = 0.5f
      // Add loading indicator text
      binding.tvClosed.text = "Won (${item.data.confirmedBid})..."
    } else {
      // Enable tab clicks when not loading
      binding.tvClosed.clickToActionWithStateSelection(HomeBidsHeaderAction_TabChangeConfirmed, item, _interface)
      binding.tvClosed.isEnabled = true
      binding.tvClosed.alpha = 1.0f
      // Remove loading indicator text
      binding.tvClosed.text = "Won (${item.data.confirmedBid})"
    }

    when(item.data.bidType){
      BidType.ActiveBid -> {
        binding.tvOngoing.isSelected = true
        binding.tvClosed.isSelected = false
      }

      BidType.ConfirmedBid -> {
        binding.tvOngoing.isSelected = false
        binding.tvClosed.isSelected = true
      }

      else -> {
        //this space is intentionally left blank
      }

    }
  }

}

/**
 * Search item view holder
 */
internal class HomeBidsSearchItemVH(binding: ViewBidsSearchbarNewItemBinding) :
  BaseHomeBidsRVAdapterViewHolder<ViewBidsSearchbarNewItemBinding, HomeBidsSearchItem>(binding) {

  private var textWatcher: android.text.TextWatcher? = null
  private var searchRunnable: Runnable? = null
  private var currentQuery: String = ""

  override fun bind(
    item: HomeBidsSearchItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {
    // Set hint for origin/destination search
    //binding.searchBar.hint = "Search by Origin or Destination"

    // Remove previous text watcher to avoid multiple listeners
    textWatcher?.let { binding.searchBar.removeTextChangedListener(it) }

    // Set up IME action listener to prevent focus crash
    binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
        // Hide keyboard and clear focus to prevent crash
        binding.searchBar.clearFocus()
        val inputMethodManager = binding.searchBar.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
        return@setOnEditorActionListener true
      }
      false
    }

    // Set up text change listener for search
    textWatcher = object : android.text.TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

      override fun afterTextChanged(s: android.text.Editable?) {
        val query = s?.toString()?.trim() ?: ""
        currentQuery = query

        // Cancel previous search runnable
        searchRunnable?.let { binding.searchBar.removeCallbacks(it) }

        if (query.isEmpty()) {
          // Clear search immediately
          _interface.handleAction(HomeBidsSearchAction_Clear, item)
        } else {
          // Debounce search with 300ms delay
          searchRunnable = Runnable {
            val searchItem = HomeBidsSearchItem(HomeBidsSearchItemData(query = query))
            _interface.handleAction(HomeBidsSearchAction_Search, searchItem)
          }
          binding.searchBar.postDelayed(searchRunnable!!, 300)
        }
      }
    }

    // Add the text watcher
    textWatcher?.let { binding.searchBar.addTextChangedListener(it) }

    // Set initial query if available, but only if different to avoid triggering text change
    item.data.query?.let { query ->
      if (binding.searchBar.text.toString() != query) {
        binding.searchBar.setText(query)
        currentQuery = query
      }
    }

    // Preserve focus if this is a rebind
    if (currentQuery.isNotEmpty()) {
      binding.searchBar.requestFocus()
    }
  }
}

/**
 * Intercity Bid request item view holder
 */
class HomeBidsRequestItemVH(binding: CardCommonBidsV2Binding) :
  BaseHomeBidsRVAdapterViewHolder<CardCommonBidsV2Binding, HomeBidsRequestItem>(binding) {
  override fun bind(
    item: HomeBidsRequestItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {
    binding.request = item.data
    // Also set request for the included layouts explicitly
    binding.containerError.request = item.data
    binding.strongBidCard.request = item.data
    binding.executePendingBindings()

    // Debug: Check live bidding visibility
    val isContract = item.data.isItContract()
    val bidStatusKey = item.data.bidStatus().statusKey
    val isLiveBidding = item.data.isLiveBidding()
    val liveBiddingVisibility = item.data.isItContractAndLiveBidding()
    val demandType = item.data.getDemandTypeByLoad()
    val requestType = item.data.requestType
    val contractBiddingEndTime = item.data.contractBiddingEndTime
    val bidEndingTime = item.data.bidEndingTime
    val isBidOpen = item.data.isBidOpen()
    
    Log.d("LiveBiddingDebug", """
        TransactionId: ${item.data.transactionId}
        IsContract: $isContract
        RequestType: $requestType
        BidStatus: $bidStatusKey
        IsBidOpen: $isBidOpen
        IsLiveBidding: $isLiveBidding
        ContractBiddingEndTime: $contractBiddingEndTime
        BidEndingTime: $bidEndingTime
        LiveBiddingVisibility: $liveBiddingVisibility (${if (liveBiddingVisibility == View.VISIBLE) "VISIBLE" else "GONE"})
        DemandType: $demandType
    """.trimIndent())

    // Manually set live bidding chip visibility (in addition to data binding)
    // This helps verify if the logic is working correctly
    try {
        val llLiveBidding = binding.root.findViewById<View>(R.id.ll_live_bidding)
        llLiveBidding?.visibility = liveBiddingVisibility
        Log.d("LiveBiddingDebug", "Manually set ll_live_bidding visibility to: ${if (liveBiddingVisibility == View.VISIBLE) "VISIBLE" else "GONE"}")
    } catch (e: Exception) {
        Log.e("LiveBiddingDebug", "Error setting live bidding visibility: ${e.message}")
    }

    // Show the place bid section for ongoing bids
    binding.containerError.clPlaceBidReportTime.visibility = View.VISIBLE

    // Handle place bid button state based on bid open status
    when(item.data.bidStatus().statusKey.lowercase()) {
      TransactionBidStatus.Open.statusKey.lowercase() -> {
        if (item.data.isBidOpen()) {
          // Bid is open - show normal "Revise To Win" button
          binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.black)
          binding.containerError.placeBidTv.text = "Revise To Win"
          binding.containerError.placeBidTv.setTextColor(
            ContextCompat.getColor(
              context,
              android.R.color.white
            )
          )
          binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_arrow_right,
            0
          )
          binding.containerError.placeBidButton.isClickable = true
          binding.containerError.placeBidButton.clickToAction(
            HomeBidsRequestAction_ReviseBid,
            item,
            _interface
          )
        } else {
          // Bid is not open - show "Awaiting Result" state
          binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_light_grey)
          binding.containerError.placeBidTv.text = "Awaiting Result"
          binding.containerError.placeBidTv.setTextColor(
            ContextCompat.getColor(
              context,
              R.color.text_grey
            )
          )
          binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_hourglass_grey,
            0,
            0,
            0
          )
          binding.containerError.placeBidButton.isClickable = false
          binding.containerError.placeBidButton.setOnClickListener(null)
        }
      }

      TransactionBidStatus.Accepted.statusKey.lowercase() -> {
        // Bid Confirmed - show confirmation button with green background
        binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.status_confirmed_bg)
        binding.containerError.placeBidTv.text = "Bid Confirmed"
        binding.containerError.placeBidTv.setTextColor(
          ContextCompat.getColor(context, R.color.bid_placed_green)
        )
        binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_check_confirmed,
          0,
          0,
          0
        )
        binding.containerError.placeBidButton.isClickable = false
        binding.containerError.placeBidButton.setOnClickListener(null)
      }

      TransactionBidStatus.Rejected.statusKey.lowercase() -> {
        // Bid Lost - show rejection button with grey background
        binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_light_grey)
        binding.containerError.placeBidTv.text = "Bid Lost"
        binding.containerError.placeBidTv.setTextColor(
          ContextCompat.getColor(context, R.color.text_grey)
        )
        binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_cross,
          0,
          0,
          0
        )
        binding.containerError.placeBidButton.isClickable = false
        binding.containerError.placeBidButton.setOnClickListener(null)
      }

      TransactionBidStatus.Cancelled.statusKey.lowercase() -> {
        // Demand Cancelled - show cancellation button with grey background
        binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_light_grey)
        binding.containerError.placeBidTv.text = "Demand Cancelled"
        binding.containerError.placeBidTv.setTextColor(
          ContextCompat.getColor(context, R.color.text_grey)
        )
        binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_cross,
          0,
          0,
          0
        )
        binding.containerError.placeBidButton.isClickable = false
        binding.containerError.placeBidButton.setOnClickListener(null)
      }

      else -> {
        //this space is intentionally left blank
      }
    }

    // Handle strongBidCard - only show for Ongoing/Active bids, hide for Closed bids
    if (item.data.bidType == BidType.ConfirmedBid) {
      // Hide strongBidCard for Closed tab (won/lost/cancelled bids)
      binding.strongBidCard.strongBidCard.visibility = View.GONE
    } else if (item.data.getSuggestedBidAmount() != null) {
      // Show bid suggestion message with custom styling (when suggested_amount is not null)
      binding.strongBidCard.strongBidCard.visibility = View.VISIBLE
      binding.strongBidCard.strongBidMessage.text = item.data.getSuggestedBidMessageValue()
      binding.strongBidCard.strongBidMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_down, 0, 0, 0)
      // Set custom background and text color for bid suggestion
      binding.strongBidCard.strongBidCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.bid_suggestion_background))
      binding.strongBidCard.strongBidMessage.setTextColor(ContextCompat.getColor(context, R.color.bid_suggestion_text))
    } else {
      // Show strong bid message (when suggested_amount is null - positive message)
      binding.strongBidCard.strongBidCard.visibility = View.VISIBLE
      binding.strongBidCard.strongBidMessage.text = item.data.getSuggestedBidMessageValue()
      binding.strongBidCard.strongBidMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_up, 0, 0, 0)
      // Keep card background color for strong bid (green)
      binding.strongBidCard.strongBidCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_confirmed_bg))
      // Reset text color to default
      binding.strongBidCard.strongBidMessage.setTextColor(ContextCompat.getColor(context, R.color.status_confirmed))
    }

    if (isContract) {
      // It's a Contract - no bolt icon for contracts
      if (demandType == "Delhivery Load") {
        binding.demandLoadType.text = "Delhivery Contract"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.delhivery_load_bg)
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
          ContextCompat.getDrawable(context, R.drawable.ic_truck_small), null, null, null
        )
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.delhivery_load_color))
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.delhivery_load_color))
      } else {
        binding.demandLoadType.text = "Client Contract"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.client_load_bg)
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
          ContextCompat.getDrawable(context, R.drawable.ic_user), null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.client_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.client_load_color))
      }
    } else {
      // It's a Load - show bolt icon only for Express loads
      if (demandType == "Delhivery Load") {
        binding.demandLoadType.text = "Delhivery Load"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.delhivery_load_bg)
        // Show bolt icon only for Express loads, otherwise show truck icon
        val iconDrawable = if (item.data.isExpress()) {
          ContextCompat.getDrawable(context, R.drawable.ic_delhivery_bolt)
        } else {
          ContextCompat.getDrawable(context, R.drawable.ic_truck)
        }
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
          iconDrawable, null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.delhivery_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.delhivery_load_color))
      } else {
        binding.demandLoadType.text = "Client Load"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.client_load_bg)
        // Show bolt icon for Express client loads, otherwise show user icon
        val iconDrawable = if (item.data.isExpress()) {
          ContextCompat.getDrawable(context, R.drawable.ic_delhivery_bolt)
        } else {
          ContextCompat.getDrawable(context, R.drawable.ic_user)
        }
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
          iconDrawable, null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.client_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.client_load_color))
      }
    }
    
    // For intercity bids: always hide distance view (distance is only shown in intracity contracts)
    binding.distance.visibility = View.GONE
    
    // Handle payment mode visibility: only show for loads (not contracts) with valid payment modes
    if (!isContract && item.data.shouldShowPaymentMode()) {
      binding.paymentType.visibility = View.VISIBLE
      // Only show advance percentage for advance payment mode
      if (item.data.shouldShowAdvancePercentage()) {
        binding.advancePaymentPercentage.visibility = View.VISIBLE
      } else {
        binding.advancePaymentPercentage.visibility = View.GONE
      }
    } else {
      binding.paymentType.visibility = View.GONE
      binding.advancePaymentPercentage.visibility = View.GONE
    }
    
    // Apply dynamic constraints AFTER visibility is set so we can calculate correct widths
    applyDynamicWidthConstraints(item.data, isContract)
  }

  /**
   * Apply dynamic text sizing to prevent overlapping on different devices
   * Measures actual text content and adjusts text size dynamically so all views fit without truncation
   * Note: truckInfo, paymentType, and advancePaymentPercentage are ALL on the same horizontal line
   */
  private fun applyDynamicWidthConstraints(data: HomeBidsRequestItemData, isContract: Boolean) {
    // Calculate available width for card content
    val availableWidth = context.calculateAvailableCardWidth(
      cardMarginDp = 12,
      contentPaddingDp = 16
    )
    
    // Check data conditions to determine what should be visible
    val shouldShowPaymentType = !isContract && data.shouldShowPaymentMode()
    val shouldShowAdvancePercentage = shouldShowPaymentType && data.shouldShowAdvancePercentage()
    
    binding.truckInfo?.let { truckInfo ->
      when {
        // All 3 views visible: truckInfo + paymentType + advancePercentage
        shouldShowAdvancePercentage -> {
          binding.paymentType?.let { paymentType ->
            binding.advancePaymentPercentage?.let { percentage ->
              adjustTextSizeToFit(
                views = listOf(truckInfo, paymentType, percentage),
                availableWidth = availableWidth,
                defaultTextSize = 14f, // From layout: android:textSize="14sp"
                minTextSize = 10f,
                spacingBetweenViews = 8.dpToPx(context),
                drawablePadding = 4.dpToPx(context)
              )
            }
          }
        }
        // Only truck + payment type (no advance percentage)
        shouldShowPaymentType -> {
          binding.paymentType?.let { paymentType ->
            adjustTextSizeToFit(
              views = listOf(truckInfo, paymentType),
              availableWidth = availableWidth,
              defaultTextSize = 14f,
              minTextSize = 10f,
              spacingBetweenViews = 8.dpToPx(context),
              drawablePadding = 4.dpToPx(context)
            )
          }
        }
        // Only truck info visible - reset to default size
        else -> {
          truckInfo.textSize = 14f // Reset to default
        }
      }
    }
  }
}


/**
 * Intracity Bid request item view holder (for both loads and contracts)
 */
class HomeBidsIntracityRequestItemVH(binding: CardCommonIntracityBidsBinding) :
  BaseHomeBidsRVAdapterViewHolder<CardCommonIntracityBidsBinding, HomeBidsRequestItem>(binding) {
  override fun bind(
    item: HomeBidsRequestItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {
    binding.request = item.data
    binding.executePendingBindings()

    val isContract = item.data.isItContract()
    val demandType = item.data.getDemandTypeByLoad()

    // Set load/contract type display
    if (isContract) {
      // It's a Contract - no bolt icon for contracts
      if (demandType == "Delhivery Load") {
        binding.demandLoadType.text = "Delhivery Contract"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.delhivery_load_bg)
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
          ContextCompat.getDrawable(context, R.drawable.ic_truck_small), null, null, null
        )
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.delhivery_load_color))
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.delhivery_load_color))
      } else {
        binding.demandLoadType.text = "Client Contract"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.client_load_bg)
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
          ContextCompat.getDrawable(context, R.drawable.ic_user), null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.client_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.client_load_color))
      }

      // For contracts: show distance, hide payment mode
      binding.distance.visibility = View.VISIBLE
      binding.paymentType.visibility = View.GONE
      binding.advancePaymentPercentage.visibility = View.GONE
    } else {
      // It's a Load - show bolt icon only for Express loads
      if (demandType == "Delhivery Load") {
        binding.demandLoadType.text = "Delhivery Load"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.delhivery_load_bg)
        // Show bolt icon only for Express loads, otherwise show truck icon
        val iconDrawable = if (item.data.isExpress()) {
          ContextCompat.getDrawable(context, R.drawable.ic_delhivery_bolt)
        } else {
          ContextCompat.getDrawable(context, R.drawable.ic_truck)
        }
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
          iconDrawable, null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.delhivery_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.delhivery_load_color))
      } else {
        binding.demandLoadType.text = "Client Load"
        binding.loadTypeLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.client_load_bg)
        // Show bolt icon for Express client loads, otherwise show user icon
        val iconDrawable = if (item.data.isExpress()) {
          ContextCompat.getDrawable(context, R.drawable.ic_delhivery_bolt)
        } else {
          ContextCompat.getDrawable(context, R.drawable.ic_user)
        }
        binding.demandLoadType.setCompoundDrawablesWithIntrinsicBounds(
          iconDrawable, null, null, null
        )
        binding.demandLoadType.setTextColor(ContextCompat.getColor(context, R.color.client_load_color))
        binding.demandLoadType.compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.client_load_color))
      }

      // For loads: show payment mode only if valid, hide distance
      binding.distance.visibility = View.GONE
      if (item.data.shouldShowPaymentMode() && !item.data.isItContract()) {
        binding.paymentType.visibility = View.VISIBLE
        // Only show advance percentage for advance payment mode
        if (item.data.shouldShowAdvancePercentage()) {
          binding.advancePaymentPercentage.visibility = View.VISIBLE
        } else {
          binding.advancePaymentPercentage.visibility = View.GONE
        }
      } else {
        binding.paymentType.visibility = View.GONE
        binding.advancePaymentPercentage.visibility = View.GONE
      }
    }
    
    // Apply dynamic constraints AFTER visibility is set so we can calculate correct widths
    applyDynamicWidthConstraints(item.data, isContract)

    // Also set request for the included layouts explicitly
    binding.containerError.request = item.data
    binding.strongBidCard.request = item.data
    
    // Handle place bid button state based on bid status
    when(item.data.bidStatus().statusKey.lowercase()) {
      TransactionBidStatus.Open.statusKey.lowercase() -> {
        if (item.data.isBidOpen()) {
          // Bid is open - show normal "Revise To Win" button
          binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.black)
          binding.containerError.placeBidTv.text = "Revise To Win"
          binding.containerError.placeBidTv.setTextColor(ContextCompat.getColor(context, android.R.color.white))
          binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0)
          binding.containerError.placeBidButton.isClickable = true
          binding.containerError.placeBidButton.clickToAction(HomeBidsRequestAction_ReviseBid, item, _interface)
        } else {
          // Bid is not open - show "Awaiting Result" state
          binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_light_grey)
          binding.containerError.placeBidTv.text = "Awaiting Result"
          binding.containerError.placeBidTv.setTextColor(ContextCompat.getColor(context, R.color.text_grey))
          binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hourglass_grey, 0, 0, 0)
          binding.containerError.placeBidButton.isClickable = false
          binding.containerError.placeBidButton.setOnClickListener(null)
        }
      }

      TransactionBidStatus.Accepted.statusKey.lowercase() -> {
        // Bid Confirmed - show confirmation button with green background
        binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.status_confirmed_bg)
        binding.containerError.placeBidTv.text = "Bid Confirmed"
        binding.containerError.placeBidTv.setTextColor(
          ContextCompat.getColor(context, R.color.bid_placed_green)
        )
        binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_check_confirmed,
          0,
          0,
          0
        )
        binding.containerError.placeBidButton.isClickable = false
        binding.containerError.placeBidButton.setOnClickListener(null)
      }

      TransactionBidStatus.Rejected.statusKey.lowercase() -> {
        // Bid Lost - show rejection button with grey background
        binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_light_grey)
        binding.containerError.placeBidTv.text = "Bid Lost"
        binding.containerError.placeBidTv.setTextColor(
          ContextCompat.getColor(context, R.color.text_grey_v3)
        )
        binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_cross,
          0,
          0,
          0
        )
        binding.containerError.placeBidButton.isClickable = false
        binding.containerError.placeBidButton.setOnClickListener(null)
      }

      TransactionBidStatus.Cancelled.statusKey.lowercase() -> {
        // Demand Cancelled - show cancellation button with grey background
        binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_light_grey)
        binding.containerError.placeBidTv.text = "Demand Cancelled"
        binding.containerError.placeBidTv.setTextColor(
          ContextCompat.getColor(context, R.color.text_grey_v3)
        )
        binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_cross,
          0,
          0,
          0
        )
        binding.containerError.placeBidButton.isClickable = false
        binding.containerError.placeBidButton.setOnClickListener(null)
      }

      else -> {
        // This is left empty
      }
    }

    // Handle strongBidCard - only show for Ongoing/Active bids, hide for Closed bids
    if (item.data.bidType == BidType.ConfirmedBid) {
      // Hide strongBidCard for Closed tab (won/lost/cancelled bids)
      binding.strongBidCard.strongBidCard.visibility = View.GONE
    } else if (item.data.getSuggestedBidAmount() != null) {
      // Show bid suggestion message with custom styling (when suggested_amount is not null)
      binding.strongBidCard.strongBidCard.visibility = View.VISIBLE
      binding.strongBidCard.strongBidMessage.text = item.data.getSuggestedBidMessageValue()
      binding.strongBidCard.strongBidMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_down, 0, 0, 0)
      // Set custom background and text color for bid suggestion
      binding.strongBidCard.strongBidCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.bid_suggestion_background))
      binding.strongBidCard.strongBidMessage.setTextColor(ContextCompat.getColor(context, R.color.bid_suggestion_text))
    } else {
      // Show strong bid message (when suggested_amount is null - positive message)
      binding.strongBidCard.strongBidCard.visibility = View.VISIBLE
      binding.strongBidCard.strongBidMessage.text = item.data.getSuggestedBidMessageValue()
      binding.strongBidCard.strongBidMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_up, 0, 0, 0)
      // Keep card background color for strong bid (green)
      binding.strongBidCard.strongBidCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_confirmed_bg))
      // Reset text color to default
      binding.strongBidCard.strongBidMessage.setTextColor(ContextCompat.getColor(context, R.color.status_confirmed))
    }
  }

  /**
   * Apply dynamic width constraints to prevent overlapping on different devices
   * Calculates available width based on screen size and applies proportional max widths
   * Accounts for spacing between views to prevent sticking
   */
  private fun applyDynamicWidthConstraints(data: HomeBidsRequestItemData, isContract: Boolean) {
    // Calculate available width for card content
    val availableWidth = context.calculateAvailableCardWidth(
      cardMarginDp = 12,  // Card margins from layout
      contentPaddingDp = 16  // Content padding from layout
    )
    
    // Account for spacing between views
    val spacingBetweenViews = 8.dpToPx(context) // Margin between city and pincode
    val actualAvailableWidth = availableWidth - spacingBetweenViews
    
    // Apply to intracity city name section (fromCity + pincode_state)
    binding.clFromToCity.findViewById<TextView>(R.id.fromCity)?.let { fromCity ->
      binding.clFromToCity.findViewById<TextView>(R.id.pincode_state)?.let { pincodeState ->
        // Distribute width with proper spacing: 62% for city name, 38% for pincode/state
        fromCity.maxWidth = (actualAvailableWidth * 0.62f).toInt()
        pincodeState.maxWidth = (actualAvailableWidth * 0.38f).toInt()
        
        // City name can wrap, pincode/state should be single line
        fromCity.ellipsize = TextUtils.TruncateAt.END
        pincodeState.ellipsize = TextUtils.TruncateAt.END
        pincodeState.maxLines = 1  // Force single line for pincode/state
      }
    }
    
    // Apply to truck info and distance/payment row based on data conditions
    applyToTruckInfoRow(availableWidth, data, isContract)
  }

  /**
   * Apply dynamic text sizing to truck info and distance/payment views
   * Measures actual text content and adjusts text size dynamically so all views fit without truncation
   * Note: truckInfo, distance/paymentType, and advancePaymentPercentage can all be on the same line
   */
  private fun applyToTruckInfoRow(availableWidth: Int, data: HomeBidsRequestItemData, isContract: Boolean) {
    // Check data conditions to determine what should be visible
    val shouldShowDistance = isContract // Distance only for contracts
    val shouldShowPaymentType = !isContract && data.shouldShowPaymentMode() && !data.isItContract()
    val shouldShowAdvancePercentage = shouldShowPaymentType && data.shouldShowAdvancePercentage()
    
    binding.truckInfo?.let { truckInfo ->
      when {
        // Distance visible (contracts): truck info + distance
        shouldShowDistance -> {
          binding.distance?.let { distance ->
            adjustTextSizeToFit(
              views = listOf(truckInfo, distance),
              availableWidth = availableWidth,
              defaultTextSize = 14f, // From layout: android:textSize="14sp"
              minTextSize = 10f,
              spacingBetweenViews = 8.dpToPx(context),
              drawablePadding = 4.dpToPx(context)
            )
          }
        }
        // Payment type + advance percentage visible (loads): all 3 on same line
        shouldShowAdvancePercentage -> {
          binding.paymentType?.let { paymentType ->
            binding.advancePaymentPercentage?.let { percentage ->
              adjustTextSizeToFit(
                views = listOf(truckInfo, paymentType, percentage),
                availableWidth = availableWidth,
                defaultTextSize = 12f, // Intracity uses 12sp for payment fields
                minTextSize = 9f,
                spacingBetweenViews = 8.dpToPx(context),
                drawablePadding = 4.dpToPx(context)
              )
            }
          }
        }
        // Only payment type visible
        shouldShowPaymentType -> {
          binding.paymentType?.let { paymentType ->
            adjustTextSizeToFit(
              views = listOf(truckInfo, paymentType),
              availableWidth = availableWidth,
              defaultTextSize = 12f,
              minTextSize = 9f,
              spacingBetweenViews = 8.dpToPx(context),
              drawablePadding = 4.dpToPx(context)
            )
          }
        }
        // Only truck info visible - reset to default size
        else -> {
          truckInfo.textSize = 14f // Reset to default
        }
      }
    }
  }
}

/**
 * Contracts Bid request item view holder
 */
class HomeContractsBidsRequestItemVH(binding: ViewContractsBidItemBinding) :
  BaseHomeBidsRVAdapterViewHolder<ViewContractsBidItemBinding, HomeContractsBidsRequestItem>(binding) {
  override fun bind(
    item: HomeContractsBidsRequestItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {
    binding.request = item.data
    if(item.data.bidStatus().statusKey.lowercase().equals("open")){
      binding.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))
      binding.tvBidStatus.text = context.resources.getString(R.string.label_under_review)
      binding.tvBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_under_review_icon, 0, 0, 0)
      binding.tvBidStatus.background = ContextCompat.getDrawable(context,R.drawable.bg_all_rounded_under_review)
      binding.tvBidStatus.compoundDrawablePadding =8
    }else if(item.data.bidStatus().statusKey.lowercase().equals("accepted")){
      binding.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.bid_placed_green))
      binding.tvBidStatus.text = context.resources.getString(R.string.label_contract_won)
      binding.tvBidStatus.background = ContextCompat.getDrawable(context,R.drawable.bg_all_rounded_won)
      binding.tvBidStatus.compoundDrawablePadding =8
      binding.tvBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_up_green, 0, 0, 0)
    }else if(item.data.bidStatus().statusKey.lowercase().equals("rejected")) {
      binding.tvBidStatus.text = context.resources.getString(R.string.label_contract_lost)
      binding.tvBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_down, 0, 0, 0)
      binding.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.destructive_red))
      binding.tvBidStatus.background = ContextCompat.getDrawable(context,R.drawable.bg_all_rounded_lost_red)
      binding.tvBidStatus.compoundDrawablePadding =8
    }else if(item.data.bidStatus().statusKey.lowercase().equals("cancelled")) {
      binding.tvBidStatus.text = context.resources.getString(R.string.cancelled)
      binding.tvBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cancel_bid, 0, 0, 0)
      binding.tvBidStatus.setTextColor(ContextCompat.getColor(context,R.color.heading_black))
      binding.tvBidStatus.background = ContextCompat.getDrawable(context,R.drawable.bg_all_round_corner_light_grey)
      binding.tvBidStatus.compoundDrawablePadding =8
    }

    val res = item.data.resOffer
    if(item.data.resOffer?.first?.first==null){
      _interface.getTotalOffers(item.data)
    }
  }
}

/**
 * Bids warning item view holder
 */
internal class HomeBidsWarningItemVH(binding: ViewWarningItemBinding) :
  BaseHomeBidsRVAdapterViewHolder<ViewWarningItemBinding, HomeBidsWarningItem>(binding) {
  override fun bind(
    item: HomeBidsWarningItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Bids timeout view holder
 */
internal class HomeBidsTimeOutItemVH(binding: ViewTimeOutItemBinding) :
  BaseHomeBidsRVAdapterViewHolder<ViewTimeOutItemBinding, HomeBidsTimeoutItem>(binding) {
  override fun bind(
    item: HomeBidsTimeoutItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Progress inline viewholder
 */
internal class HomeBidsProgressItemVH(binding: ViewHomeBidsProgressItemBinding) :
  BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsProgressItemBinding, HomeBidsProgressItem>(
    binding
  ) {
  override fun bind(
    item: HomeBidsProgressItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {

  }
}

/**
 * Marketplace Bid request item view holder (for spot request type)
 */
class HomeBidsMarketplaceRequestItemVH(binding: CardBidsDelhiveryMarketplaceBinding) :
  BaseHomeBidsRVAdapterViewHolder<CardBidsDelhiveryMarketplaceBinding, HomeBidsRequestItem>(binding) {
  
  private var callLoadingState: Boolean = false
  
  override fun bind(
    item: HomeBidsRequestItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {
    binding.request = item.data
    binding.executePendingBindings()

    // Set up call icon container click listener (not individual icon)
    binding.containerError.callIconContainer.clickToAction(
      HomeBidsRequestAction_InitiateCall,
      item,
      _interface
    )
    
    // Update progress bar visibility based on loading state
    updateCallProgressBar(callLoadingState)
    
    // Apply dynamic constraints AFTER data binding
    applyDynamicWidthConstraints(item.data)

    // Handle place bid button state based on bid status
    when(item.data.bidStatus().statusKey.lowercase()) {
      TransactionBidStatus.Open.statusKey.lowercase() -> {
        Log.d("Bidding-End-Time", "$adapterPosition,${item.data.isBidOpen()}")
        if (item.data.isMarketplaceBidOpen()) {
          // Bid is open - show normal "Revise to Win" button
          binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.black)
          binding.containerError.placeBidTv.text = "Revise to Win"
          binding.containerError.placeBidTv.setTextColor(
            ContextCompat.getColor(context, android.R.color.white)
          )
          binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
            0, 0, R.drawable.ic_arrow_right, 0
          )
          binding.containerError.placeBidButton.isClickable = true
          binding.containerError.placeBidButton.clickToAction(
            HomeBidsRequestAction_ReviseBid,
            item,
            _interface
          )
        } else {
          // Bid is not open - show "Awaiting Result" state (consistent with other bid types)
          binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_light_grey)
          binding.containerError.placeBidTv.text = "Awaiting Result"
          binding.containerError.placeBidTv.setTextColor(
            ContextCompat.getColor(context, R.color.text_grey)
          )
          binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_hourglass_grey, 0, 0, 0
          )
          binding.containerError.placeBidButton.isClickable = false
          binding.containerError.placeBidButton.setOnClickListener(null)
        }
      }

      TransactionBidStatus.Accepted.statusKey.lowercase() -> {
        // Bid Confirmed - show confirmation button with green background
        binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.status_confirmed_bg)
        binding.containerError.placeBidTv.text = "Bid Confirmed"
        binding.containerError.placeBidTv.setTextColor(
          ContextCompat.getColor(context, R.color.bid_placed_green)
        )
        binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_check_confirmed, 0, 0, 0
        )
        binding.containerError.placeBidButton.isClickable = false
        binding.containerError.placeBidButton.setOnClickListener(null)
      }

      TransactionBidStatus.Rejected.statusKey.lowercase() -> {
        // Bid Lost - show rejection button with grey background
        binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_light_grey)
        binding.containerError.placeBidTv.text = "Bid Lost"
        binding.containerError.placeBidTv.setTextColor(
          ContextCompat.getColor(context, R.color.text_grey)
        )
        binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_cross, 0, 0, 0
        )
        binding.containerError.placeBidButton.isClickable = false
        binding.containerError.placeBidButton.setOnClickListener(null)
      }

      TransactionBidStatus.Cancelled.statusKey.lowercase() -> {
        // Demand Cancelled - show cancellation button with grey background
        binding.containerError.placeBidButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_light_grey)
        binding.containerError.placeBidTv.text = "Demand Cancelled"
        binding.containerError.placeBidTv.setTextColor(
          ContextCompat.getColor(context, R.color.text_grey)
        )
        binding.containerError.placeBidTv.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_cross, 0, 0, 0
        )
        binding.containerError.placeBidButton.isClickable = false
        binding.containerError.placeBidButton.setOnClickListener(null)
      }

      else -> {
        // Default state
      }
    }
  }
  
  /**
   * Update call button loading state
   */
  fun setCallLoadingState(isLoading: Boolean) {
    callLoadingState = isLoading
    updateCallProgressBar(isLoading)
  }
  
  /**
   * Update progress bar and icon visibility
   */
  private fun updateCallProgressBar(isLoading: Boolean) {
    if (isLoading) {
      // Show progress bar, hide icon, disable clicks
      binding.containerError.callProgressBar.visibility = View.VISIBLE
      binding.containerError.callIcon.visibility = View.GONE
      binding.containerError.callIconContainer.isClickable = false
      binding.containerError.callIconContainer.isFocusable = false
    } else {
      // Hide progress bar, show icon, enable clicks
      binding.containerError.callProgressBar.visibility = View.GONE
      binding.containerError.callIcon.visibility = View.VISIBLE
      binding.containerError.callIconContainer.isClickable = true
      binding.containerError.callIconContainer.isFocusable = true
    }
  }

  /**
   * Apply dynamic text sizing to prevent truncation on different devices
   */
  private fun applyDynamicWidthConstraints(data: HomeBidsRequestItemData) {
    // Calculate available width for card content
    val availableWidth = context.calculateAvailableCardWidth(
      cardMarginDp = 12,
      contentPaddingDp = 16
    )
    
    // Determine visibility based on data
    val shouldShowPaymentType = data.shouldShowMarketplacePaymentMode()
    val shouldShowAdvancePercentage = data.shouldShowMarketplaceAdvancePercentage()
    
    // Apply dynamic text sizing to truck info + payment row
    binding.truckInfo?.let { truckInfo ->
      when {
        // All 3 views visible
        shouldShowAdvancePercentage -> {
          binding.paymentType?.let { paymentType ->
            binding.advancePaymentPercentage?.let { percentage ->
              adjustTextSizeToFit(
                views = listOf(truckInfo, paymentType, percentage),
                availableWidth = availableWidth,
                defaultTextSize = 14f,
                minTextSize = 10f,
                spacingBetweenViews = 8.dpToPx(context),
                drawablePadding = 4.dpToPx(context)
              )
            }
          }
        }
        // Only truck + payment type
        shouldShowPaymentType -> {
          binding.paymentType?.let { paymentType ->
            adjustTextSizeToFit(
              views = listOf(truckInfo, paymentType),
              availableWidth = availableWidth,
              defaultTextSize = 14f,
              minTextSize = 10f,
              spacingBetweenViews = 8.dpToPx(context),
              drawablePadding = 4.dpToPx(context)
            )
          }
        }
        // Only truck info - reset to default
        else -> {
          truckInfo.textSize = 14f
        }
      }
    }
  }
}