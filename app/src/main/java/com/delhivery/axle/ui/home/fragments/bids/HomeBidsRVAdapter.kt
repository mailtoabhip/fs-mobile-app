package com.delhivery.axle.ui.home.fragments.bids

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.bids.HomeBidsHeaderItemData
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.CardCommonBidsBinding
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

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeBidsRVAdapterItemType.byTypeId(viewType)) {
    //Header -> ViewHomeBidsHeaderItemBinding.inflate(inflater, parent, false)
    Header -> ViewBidsHeaderNewItemBinding.inflate(inflater, parent, false)
    //Search -> ViewHomeSearchItemBinding.inflate(inflater, parent, false)
    Search -> ViewBidsSearchbarNewItemBinding.inflate(inflater, parent, false)
    //load bids
    Request -> CardCommonBidsBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeBidsProgressItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    //contract bids
    Contracts -> CardCommonBidsBinding.inflate(inflater, parent, false)
    //load bids
    else -> CardCommonBidsBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewBidsHeaderNewItemBinding -> HomeBidsHeaderItemVH(binding)
    is ViewBidsSearchbarNewItemBinding -> HomeBidsSearchItemVH(binding)
    //load bids + contract bids
    is CardCommonBidsBinding -> HomeBidsRequestItemVH(binding)
    //
    is ViewWarningItemBinding -> HomeBidsWarningItemVH(binding)
    is ViewTimeOutItemBinding -> HomeBidsTimeOutItemVH(binding)
    is ViewHomeBidsProgressItemBinding -> HomeBidsProgressItemVH(binding)
    //contract bids
    //is ViewContractsBidItemBinding -> HomeContractsBidsRequestItemVH(binding)
    //else -> //load bids + contract bids
    else -> HomeBidsRequestItemVH(binding as CardCommonBidsBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeBidsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeBidsHeaderItemVH -> holder.bind(item as HomeBidsHeaderItem, _interface)
      is HomeBidsSearchItemVH -> holder.bind(item as HomeBidsSearchItem, _interface)
      //load bids + //contract bids
      is HomeBidsRequestItemVH -> holder.bind(item as HomeBidsRequestItem, _interface)
      is HomeBidsWarningItemVH -> holder.bind(item as HomeBidsWarningItem, _interface)
      is HomeBidsTimeOutItemVH -> holder.bind(item as HomeBidsTimeoutItem, _interface)
      //contract bids
      //is HomeContractsBidsRequestItemVH -> holder.bind(item as HomeContractsBidsRequestItem, _interface)
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
   * Reset all data, remove all errors/transactions
   */
  //fun resetStaticData(bidType: BidType = BidType.ActiveBid) {
  fun resetStaticData(activeBidCount:String = "0", confirmedBidCount:String = "0", lostBidCount:String = "0", bidType: BidType = BidType.ActiveBid) {
    mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      //commented this out to avoid the wrong tab selection during refresh if any other tab is clicked apart from the default one i.e "Ongoing"
      add(Pair(HomeBidsHeaderItem(HomeBidsHeaderItemData(myBids = Integer.parseInt(activeBidCount), confirmedBid = Integer.parseInt(confirmedBidCount), lostBids = Integer.parseInt(lostBidCount), bidType = bidType)), Update))
      add(Pair(HomeBidsProgressItem(), AddUpdate))
      items.filter { it.type == Request || it.type == Warning || it.type == Timeout || it.type == Search || it.type==Contracts }
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