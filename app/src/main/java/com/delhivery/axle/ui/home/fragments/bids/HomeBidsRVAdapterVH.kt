package com.delhivery.axle.ui.home.fragments.bids

import android.util.Log
import androidx.databinding.ViewDataBinding
import android.view.View
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.home.bids.*
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
    } else if (item.data.getBidSuggestionValue()) {
      // Show bid suggestion message with custom styling
      binding.strongBidCard.strongBidCard.visibility = View.VISIBLE
      binding.strongBidCard.strongBidMessage.text = item.data.getSuggestedBidMessageValue()
      binding.strongBidCard.strongBidMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_down, 0, 0, 0)
      // Set custom background and text color for bid suggestion
      binding.strongBidCard.strongBidCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.bid_suggestion_background))
      binding.strongBidCard.strongBidMessage.setTextColor(ContextCompat.getColor(context, R.color.bid_suggestion_text))
    } else {
      // Show strong bid message (leave as is - default from XML)
      binding.strongBidCard.strongBidCard.visibility = View.VISIBLE
      binding.strongBidCard.strongBidMessage.text = "You have high chances of winning!"
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
    } else if (item.data.getBidSuggestionValue()) {
      // Show bid suggestion message with custom styling
      binding.strongBidCard.strongBidCard.visibility = View.VISIBLE
      binding.strongBidCard.strongBidMessage.text = item.data.getSuggestedBidMessageValue()
      binding.strongBidCard.strongBidMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_down, 0, 0, 0)
      // Set custom background and text color for bid suggestion
      binding.strongBidCard.strongBidCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.bid_suggestion_background))
      binding.strongBidCard.strongBidMessage.setTextColor(ContextCompat.getColor(context, R.color.bid_suggestion_text))
    } else {
      // Show strong bid message (leave as is - default from XML)
      binding.strongBidCard.strongBidCard.visibility = View.VISIBLE
      binding.strongBidCard.strongBidMessage.text = "You have high chances of winning!"
      binding.strongBidCard.strongBidMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_up, 0, 0, 0)
      // Keep card background color for strong bid (green)
      binding.strongBidCard.strongBidCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_confirmed_bg))
      // Reset text color to default
      binding.strongBidCard.strongBidMessage.setTextColor(ContextCompat.getColor(context, R.color.status_confirmed))
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