package com.delhivery.axle.ui.home.fragments.bids

import android.util.Log
import androidx.databinding.ViewDataBinding
import android.view.View
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.home.bids.*
import com.delhivery.axle.databinding.CardCommonBidsBinding
import com.delhivery.axle.databinding.LoadDelhiveryIntercityBinding
import com.delhivery.axle.databinding.ViewBidsHeaderNewItemBinding
import com.delhivery.axle.databinding.ViewBidsSearchbarNewItemBinding
import com.delhivery.axle.databinding.ViewContractsBidItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.utils.extensions.underline

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
    binding.wonCount = item.data.confirmedBid
    if (isLoadingData) {
      // Disable tab clicks when loading
      binding.tvWon.setOnClickListener(null)
      binding.tvWon.isEnabled = false
      //binding.tvWon.alpha = 0.5f
      // Add loading indicator text
      binding.tvWon.text = "Won (${item.data.confirmedBid})..."
    } else {
      // Enable tab clicks when not loading
      binding.tvWon.clickToActionWithStateSelection(HomeBidsHeaderAction_TabChangeConfirmed, item, _interface)
      binding.tvWon.isEnabled = true
      binding.tvWon.alpha = 1.0f
      // Remove loading indicator text
      binding.tvWon.text = "Won (${item.data.confirmedBid})"
    }

    //
    binding.lostCount = item.data.lostBids
    if (isLoadingData) {
      // Disable tab clicks when loading
      binding.tvLost.setOnClickListener(null)
      binding.tvLost.isEnabled = false
      //binding.tvLost.alpha = 0.5f
      // Add loading indicator text
      binding.tvLost.text = "Lost (${item.data.lostBids})..."
    } else {
      // Enable tab clicks when not loading
      binding.tvLost.clickToActionWithStateSelection(HomeBidsHeaderAction_TabChangeLost, item, _interface)
      binding.tvLost.isEnabled = true
      binding.tvLost.alpha = 1.0f
      // Remove loading indicator text
      binding.tvLost.text = "Lost (${item.data.lostBids})"
    }

    when(item.data.bidType){
      BidType.ActiveBid -> {
        binding.tvOngoing.isSelected = true
        binding.tvWon.isSelected = false
        binding.tvLost.isSelected = false
      }

      BidType.ConfirmedBid -> {
        binding.tvOngoing.isSelected = false
        binding.tvWon.isSelected = true
        binding.tvLost.isSelected = false
      }

      BidType.LostBid -> {
        binding.tvOngoing.isSelected = false
        binding.tvWon.isSelected = false
        binding.tvLost.isSelected = true
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
 * Bid request item view holder
 */
class HomeBidsRequestItemVH(binding: CardCommonBidsBinding) :
    BaseHomeBidsRVAdapterViewHolder<CardCommonBidsBinding, HomeBidsRequestItem>(binding) {
  override fun bind(
    item: HomeBidsRequestItem,
    _interface: HomeBidsRVAdapterInterface,
    isLoadingData: Boolean
  ) {
    binding.request = item.data

      //hide the placebid section for Bids
      binding.includeBidTime6.containerError.clPlaceBidReportTime.visibility = View.GONE

    //binding.includeBidTime6.tvReviseBid.clickToAction(HomeBidsRequestAction_ReviseBid, item, _interface)
    //set button or bid status visibility based on ongoing/ won/ lost tab and bid cancelled/ bid lost/ awaiting result

    when(item.data.bidStatus().statusKey.lowercase()){
      TransactionBidStatus.Open.statusKey.lowercase() -> {
        if(item.data.isBidOpen()){
          //Bid Open status - Can be revised
          binding.includeBidTime6.tvReviseBid.visibility = View.VISIBLE
          binding.includeBidTime6.tvReviseBid.clickToAction(HomeBidsRequestAction_ReviseBid, item, _interface)
          binding.includeBidTime6.ivBidStatus.visibility = View.GONE
          binding.includeBidTime6.tvBidStatus.visibility = View.GONE
        }else{
          //Awaiting Result status - can't be revised
          binding.includeBidTime6.tvReviseBid.visibility = View.GONE
          binding.includeBidTime6.ivBidStatus.visibility = View.VISIBLE
          binding.includeBidTime6.tvBidStatus.visibility = View.VISIBLE

          //set image
          binding.includeBidTime6.ivBidStatus.setImageResource(R.drawable.ic_wait)
          //set text
          binding.includeBidTime6.tvBidStatus.text = "Awaiting Result"
          binding.includeBidTime6.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.orange_v3))
        }
      }

      TransactionBidStatus.Accepted.statusKey.lowercase() -> {
        binding.includeBidTime6.tvReviseBid.visibility = View.GONE
        binding.includeBidTime6.ivBidStatus.visibility = View.VISIBLE
        binding.includeBidTime6.tvBidStatus.visibility = View.VISIBLE
        //set image
        binding.includeBidTime6.ivBidStatus.setImageResource(R.drawable.ic_check_confirmed)
        //set text
        binding.includeBidTime6.tvBidStatus.text = "Bid Confirmed"
        binding.includeBidTime6.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.bid_placed_green))
      }

      TransactionBidStatus.Rejected.statusKey.lowercase() -> {
        binding.includeBidTime6.tvReviseBid.visibility = View.GONE
        binding.includeBidTime6.ivBidStatus.visibility = View.VISIBLE
        binding.includeBidTime6.tvBidStatus.visibility = View.VISIBLE
        //set image
        binding.includeBidTime6.ivBidStatus.setImageResource(R.drawable.ic_cross)
        //set text
        binding.includeBidTime6.tvBidStatus.text = "Bid Lost"
        binding.includeBidTime6.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.text_grey_v3))
      }

      TransactionBidStatus.Cancelled.statusKey.lowercase() -> {
        binding.includeBidTime6.tvReviseBid.visibility = View.GONE
        binding.includeBidTime6.ivBidStatus.visibility = View.VISIBLE
        binding.includeBidTime6.tvBidStatus.visibility = View.VISIBLE
        //set image
        binding.includeBidTime6.ivBidStatus.setImageResource(R.drawable.ic_cross)
        //set text
        binding.includeBidTime6.tvBidStatus.text = "Demand Cancelled"
        binding.includeBidTime6.tvBidStatus.setTextColor(ContextCompat.getColor(context, R.color.text_grey_v3))
      }

      else -> {
        //this space is intentionally left blank
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