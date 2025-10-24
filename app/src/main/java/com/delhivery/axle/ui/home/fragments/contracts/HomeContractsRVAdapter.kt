package com.delhivery.axle.ui.home.fragments.contracts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Contracts
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Filters
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.IntracityFilters
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Warning
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Search

class HomeContractsRVAdapter (private val _interface: HomeContractsRVAdapterInterface) :
  BaseDataRVAdapter<BaseHomeContractsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
    _interface
  ) {

  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getItemViewType(position: Int): Int {
    val item = items[position]
    // Check if it's a contract request item and if it's intracity
    return if (item is HomeContractsRequestItem && item.data.isItIntraCityContract()) {
      HomeContractsRVAdapterItemType.IntracityContracts.typeId
    } else {
      item.type.typeId
    }
  }

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeContractsRVAdapterItemType.byTypeId(viewType)) {
    Search -> ViewSearchContractsItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeContractsProgressItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    Filters -> ViewHomeContractsFilterItemBinding.inflate(inflater, parent, false)
    IntracityFilters-> ViewHomeContractsIntracityFilterItemBinding.inflate(inflater, parent, false)
    HomeContractsRVAdapterItemType.IntracityContracts -> CardsContractsIntracityTripsBidsBinding.inflate(inflater, parent, false)  // Use intracity layout
    else -> CardContractsIntercityTripsBidsBinding.inflate(inflater, parent, false)  // Use intercity layout
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewSearchContractsItemBinding -> HomeContractsSearchItemVH(binding)
    is ViewHomeContractsProgressItemBinding -> HomeContractsProgressItemVH(binding)
    is ViewWarningItemBinding -> HomeContractsWarningItemVH(binding)
    is ViewTimeOutItemBinding -> HomeContractsTimeOutItemVH(binding)
    is ViewHomeContractsFilterItemBinding -> HomeContractsFilterItemVH(binding)
    is ViewHomeContractsIntracityFilterItemBinding -> HomeContractsIntracityFilterItemVH(binding)
    is CardsContractsIntracityTripsBidsBinding -> HomeContractsIntracityRequestItemVH(binding)  // Intracity contracts
    is CardContractsIntercityTripsBidsBinding -> HomeContractsRequestItemVH(binding)  // Intercity contracts
    else -> HomeContractsRequestItemVH(binding as CardContractsIntercityTripsBidsBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeContractsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeContractsSearchItemVH -> holder.bind(item as HomeContractsSearchItem, _interface)
      is HomeContractsIntracityRequestItemVH -> holder.bind(item as HomeContractsRequestItem, _interface)  // Intracity contracts
      is HomeContractsRequestItemVH -> holder.bind(item as HomeContractsRequestItem, _interface)  // Intercity contracts
      is HomeContractsProgressItemVH -> holder.bind(item as HomeContractsProgressItem, _interface)
      is HomeContractsWarningItemVH -> holder.bind(item as HomeContractsWarningItem, _interface)
      is HomeContractsTimeOutItemVH -> holder.bind(item as HomeContractsTimeoutItem, _interface)
      is HomeContractsFilterItemVH -> holder.bind(item as HomeContractsFilterItem, _interface)
      is HomeContractsIntracityFilterItemVH -> holder.bind(item as HomeContractsIntracityFilterItem, _interface)
    }
  }

  /**
   * Remove info/warning/timeout data
   */
  fun removeInfoData() {
    mutableListOf<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeContractsProgressItem(), AddUpdate))
      items.filter {
        it.type == Warning || it.type == Timeout
      }
        .map { Pair(it, Remove) }
        .let {
          addAll(it)
        }
    }
      .let {
        operation(it)
      }
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomeContractsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeContractsProgressItem(), AddUpdate))
      items.filter {
        it.type == Contracts || it.type == Warning || it.type == Timeout || it.type ==Filters ||  it.type == Search || it.type == IntracityFilters
      }
        .map { Pair(it, Remove) }
        .let {
          addAll(it)
        }
    }
      .let {
        operation(it)
      }
  }

  override fun onViewRecycled(holder: BaseViewHolder<*>) {
    super.onViewRecycled(holder)
    when (holder) {
      is HomeContractsRequestItemVH -> holder.stopCounter()
      is HomeContractsIntracityRequestItemVH -> holder.stopCounter()
    }
  }
}