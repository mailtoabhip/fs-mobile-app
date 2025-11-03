package com.delhivery.axle.ui.home.fragments.bids

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.bids.HomeBidsHeaderItemData
import com.delhivery.axle.api.repository.RequestType
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.SUB_REQUEST_TYPE_INTRACITY
import com.delhivery.axle.databinding.CardBidsDelhiveryMarketplaceBinding
import com.delhivery.axle.databinding.CardCommonBidsBinding
import com.delhivery.axle.databinding.CardCommonBidsV2Binding
import com.delhivery.axle.databinding.CardCommonIntracityBidsBinding
import com.delhivery.axle.databinding.LoadDelhiveryIntercityBinding
import com.delhivery.axle.databinding.ViewBidsHeaderNewItemBinding
import com.delhivery.axle.databinding.ViewBidsSearchbarNewItemBinding
import com.delhivery.axle.databinding.ViewContractsBidItemBinding
import com.delhivery.axle.databinding.ViewContractsBidResultsBinding
import com.delhivery.axle.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Contracts
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Header
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.IntracityBids
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.MarketplaceBids
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Warning
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * RV adapter for [HomeBidsFragment]
 */
class HomeBidsRVAdapter(private val _interface: HomeBidsRVAdapterInterface) :
    BaseFilterableDataRVAdapter<BaseHomeBidsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  private var currentFilterQuery: String? = null
  private var isLoadingData: Boolean = false

  override fun getItemViewType(position: Int): Int {
    val item = itemsList()[position]
    // Check if it's a bid request item
    return if (item is HomeBidsRequestItem) {
      // First check for marketplace/spot request type
      if (isMarketplaceBid(item.data)) {
        MarketplaceBids.typeId
      } 
      // Then check if it's intracity
      else if (isIntracity(item.data)) {
        IntracityBids.typeId
      } else {
        item.type.typeId
      }
    } else {
      item.type.typeId
    }
  }

  /**
   * Check if a bid request is marketplace/spot type
   */
  private fun isMarketplaceBid(data: HomeBidsRequestItemData): Boolean {
    return data.requestType?.lowercase() == RequestType.SpotMarketplace.type
  }

  /**
   * Check if a bid request is intracity (for both loads and contracts)
   */
  private fun isIntracity(data: HomeBidsRequestItemData): Boolean {
    // Check for intracity loads
    if (data.subRequestType == SUB_REQUEST_TYPE_INTRACITY) {
      return true
    }
    // Check for intracity contracts
    if (data.isItIntraCityContract()) {
      return true
    }
    return false
  }

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeBidsRVAdapterItemType.byTypeId(viewType)) {
    //Header -> ViewHomeBidsHeaderItemBinding.inflate(inflater, parent, false)
    Header -> ViewBidsHeaderNewItemBinding.inflate(inflater, parent, false)
    //Search -> ViewHomeSearchItemBinding.inflate(inflater, parent, false)
    Search -> ViewBidsSearchbarNewItemBinding.inflate(inflater, parent, false)
    //load bids - using new v2 layout
    Request -> CardCommonBidsV2Binding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeBidsProgressItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    //contract bids - using new v2 layout
    Contracts -> CardCommonBidsV2Binding.inflate(inflater, parent, false)
    //intracity bids (loads + contracts) - using new intracity layout
    IntracityBids -> CardCommonIntracityBidsBinding.inflate(inflater, parent, false)
    //marketplace bids (spot request type) - using marketplace layout
    MarketplaceBids -> CardBidsDelhiveryMarketplaceBinding.inflate(inflater, parent, false)
    //load bids - using new v2 layout
    else -> CardCommonBidsV2Binding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewBidsHeaderNewItemBinding -> HomeBidsHeaderItemVH(binding)
    is ViewBidsSearchbarNewItemBinding -> HomeBidsSearchItemVH(binding)
    //load bids + contract bids - using new v2 binding
    is CardCommonBidsV2Binding -> HomeBidsRequestItemVH(binding)
    //intracity bids (loads + contracts) - using intracity binding
    is CardCommonIntracityBidsBinding -> HomeBidsIntracityRequestItemVH(binding)
    //marketplace bids (spot request type) - using marketplace binding
    is CardBidsDelhiveryMarketplaceBinding -> HomeBidsMarketplaceRequestItemVH(binding)
    //
    is ViewWarningItemBinding -> HomeBidsWarningItemVH(binding)
    is ViewTimeOutItemBinding -> HomeBidsTimeOutItemVH(binding)
    is ViewHomeBidsProgressItemBinding -> HomeBidsProgressItemVH(binding)
    //contract bids
    //is ViewContractsBidItemBinding -> HomeContractsBidsRequestItemVH(binding)
    //else -> //load bids + contract bids - using new v2 binding
    else -> HomeBidsRequestItemVH(binding as CardCommonBidsV2Binding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeBidsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeBidsHeaderItemVH -> holder.bind(item as HomeBidsHeaderItem, _interface, isLoadingData)
      is HomeBidsSearchItemVH -> holder.bind(item as HomeBidsSearchItem, _interface, isLoadingData)
      //load bids + //contract bids
      is HomeBidsRequestItemVH -> holder.bind(item as HomeBidsRequestItem, _interface, isLoadingData)
      //intracity bids (loads + contracts)
      is HomeBidsIntracityRequestItemVH -> holder.bind(item as HomeBidsRequestItem, _interface, isLoadingData)
      //marketplace bids (spot request type)
      is HomeBidsMarketplaceRequestItemVH -> holder.bind(item as HomeBidsRequestItem, _interface, isLoadingData)
      is HomeBidsWarningItemVH -> holder.bind(item as HomeBidsWarningItem, _interface, isLoadingData)
      is HomeBidsTimeOutItemVH -> holder.bind(item as HomeBidsTimeoutItem, _interface, isLoadingData)
      //contract bids
      is HomeContractsBidsRequestItemVH -> holder.bind(item as HomeContractsBidsRequestItem, _interface, isLoadingData)
    }
  }

  override fun filterList(query: String) =
    items.filter { item ->
      when (item.type) {
        Search -> {
          android.util.Log.d("SearchDebug", "Keeping search item")
          true // Always show search item
        }
        else -> {
          // Filter by origin or destination for bid items
          if (item is HomeBidsRequestItem) {
            val data = item.data as HomeBidsRequestItemData
            val searchQuery = query.lowercase()

            // Check origin fields
            val originMatch = data.origin?.lowercase()?.contains(searchQuery)?:false ||
                    data.originCity?.lowercase()?.contains(searchQuery) == true ||
                    data.originCenterName?.lowercase()?.contains(searchQuery) == true ||
                    data.pickupLocation?.lowercase()?.contains(searchQuery)?:false ||
                    data.pickupLocationCity?.lowercase()?.contains(searchQuery) == true

            // Check destination fields
            val destinationMatch = data.destination?.lowercase()?.contains(searchQuery)?:false ||
                    data.destinationCityCode?.lowercase()?.contains(searchQuery) == true ||
                    data.dropLocationCity?.lowercase()?.contains(searchQuery) == true

            val matches = originMatch || destinationMatch
            android.util.Log.d("SearchDebug", "Item ${data.origin} -> ${data.destination} matches: $matches")
            matches
          } else {
            // For other item types, use default filter
            val matches = item.data.filter(query)
            android.util.Log.d("SearchDebug", "Other item type ${item.type} matches: $matches")
            matches
          }
        }
      }
    }

  override fun filter(query: String?): Boolean {
    android.util.Log.d("SearchDebug", "Filter called with query: $query")
    currentFilterQuery = query

    if (query.isNullOrEmpty()) {
      // Clear filter
      android.util.Log.d("SearchDebug", "Clearing filter")
      cancelFilter()
      return true
    }

    // Enable filtering mode
    enableFilter()
    android.util.Log.d("SearchDebug", "Enabled filtering mode")

    // Get the search item to preserve it
    val searchItem = items.find { it.type == Search }
    android.util.Log.d("SearchDebug", "Found search item: ${searchItem != null}")

    // Apply the filter using base class method
    val result = super.filter(query)
    android.util.Log.d("SearchDebug", "Base filter result: $result")

    // Ensure search item is always at the top of filtered items
    if (searchItem != null && !filteredItems.any { it.type == Search }) {
      filteredItems.add(0, searchItem)
      android.util.Log.d("SearchDebug", "Added search item to filtered list")
    }

    android.util.Log.d("SearchDebug", "Final filtered items size: ${filteredItems.size}")

    return filteredItems.isNotEmpty()
  }

  override fun cancelFilter() {
    android.util.Log.d("SearchDebug", "Canceling filter")
    currentFilterQuery = null
    super.cancelFilter()
  }

  fun getCurrentFilterQuery(): String? = currentFilterQuery

  /**
   * Update loading state to disable tab clicks during data fetch
   */
  fun setLoadingState(loading: Boolean) {
    isLoadingData = loading
    // Notify all items to update their states
    notifyDataSetChanged()
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  //fun resetStaticData(bidType: BidType = BidType.ActiveBid) {
  fun resetStaticData(activeBidCount:String = "0", confirmedBidCount:String = "0", lostBidCount:String = "0", bidType: BidType = BidType.ActiveBid) {
    mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      //commented this out to avoid the wrong tab selection during refresh if any other tab is clicked apart from the default one i.e "Ongoing"
      add(Pair(HomeBidsHeaderItem(HomeBidsHeaderItemData(myBids = Integer.parseInt(activeBidCount), confirmedBid = Integer.parseInt(confirmedBidCount), lostBids = Integer.parseInt(lostBidCount), bidType = bidType)), Update))
      add(Pair(HomeBidsProgressItem(), AddUpdate))
      items.filter { it.type == Request || it.type == Warning || it.type == Timeout || it.type == Search || it.type==Contracts || it.type==IntracityBids || it.type==MarketplaceBids }
          .map { Pair(it, Remove) }
          .let {
            addAll(it)
          }
    }
        .let {
          operation(it)
        }
  }

  override fun enableFilter() {
    android.util.Log.d("SearchDebug", "Enabling filter")
    super.enableFilter()
    isFiltering = true
  }
}