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

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeContractsRVAdapterItemType.byTypeId(viewType)) {
    Search -> ViewHomeLoadsSearchItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeContractsProgressItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    Filters -> ViewHomeContractsFilterItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeContractsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeLoadsSearchItemBinding -> HomeContractsSearchItemVH(binding)
    is ViewHomeContractsProgressItemBinding -> HomeContractsProgressItemVH(binding)
    is ViewWarningItemBinding -> HomeContractsWarningItemVH(binding)
    is ViewTimeOutItemBinding -> HomeContractsTimeOutItemVH(binding)
    is ViewHomeContractsFilterItemBinding -> HomeContractsFilterItemVH(binding)
    else -> HomeContractsRequestItemVH(binding as ViewHomeContractsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeContractsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeContractsSearchItemVH -> holder.bind(item as HomeContractsSearchItem, _interface)
      is HomeContractsRequestItemVH -> holder.bind(item as HomeContractsRequestItem, _interface)
      is HomeContractsProgressItemVH -> holder.bind(item as HomeContractsProgressItem, _interface)
      is HomeContractsWarningItemVH -> holder.bind(item as HomeContractsWarningItem, _interface)
      is HomeContractsTimeOutItemVH -> holder.bind(item as HomeContractsTimeoutItem, _interface)
      is HomeContractsFilterItemVH -> holder.bind(item as HomeContractsFilterItem, _interface)
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
        it.type == Contracts || it.type == Warning || it.type == Timeout || it.type ==Filters ||  it.type == Search
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
}