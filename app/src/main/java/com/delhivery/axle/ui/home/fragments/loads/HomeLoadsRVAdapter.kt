package com.delhivery.axle.ui.home.fragments.loads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.*
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Filters
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Info
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.MoreInfo
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Warning
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Banners
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Priority



class HomeLoadsRVAdapter(private val _interface: HomeLoadsRVAdapterInterface) :
    BaseDataRVAdapter<BaseHomeLoadsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeLoadsRVAdapterItemType.byTypeId(viewType)) {
    Search -> ViewHomeLoadsSearchItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeLoadsProgressItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Info -> ViewHomeLoadsInfoItemBinding.inflate(inflater, parent, false)
    MoreInfo -> ViewHomeLoadsMoreInfoItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    Filters -> ViewHomeLoadsFilterItemBinding.inflate(inflater, parent, false)
    Count -> ViewHomeSummaryItemBinding.inflate(inflater, parent, false)
    Banners -> ViewHomeLoadsTruckBannerItemBinding.inflate(inflater, parent, false)
   Priority -> ViewHomeLoadsTruckPriorityItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeLoadsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeLoadsSearchItemBinding -> HomeLoadsSearchItemVH(binding)
    is ViewHomeLoadsProgressItemBinding -> HomeLoadsProgressItemVH(binding)
    is ViewWarningItemBinding -> HomeLoadsWarningItemVH(binding)
    is ViewHomeLoadsInfoItemBinding -> HomeLoadsInfoItemVH(binding)
    is ViewHomeLoadsMoreInfoItemBinding -> HomeLoadsMoreInfoItemVH(binding)
    is ViewTimeOutItemBinding -> HomeLoadsTimeOutItemVH(binding)
    is ViewHomeLoadsFilterItemBinding -> HomeLoadsFilterItemVH(binding)
    is ViewHomeSummaryItemBinding -> HomeLoadsMoreInfoItemVH.HomeLoadsSummaryItemVH(binding)
    is ViewHomeLoadsTruckBannerItemBinding->HomeLoadsAddTruckItemVH(binding)
    is ViewHomeLoadsTruckPriorityItemBinding->HomeLoadsTruckPriorityItemVH(binding)
    else -> HomeLoadsRequestItemVH(binding as ViewHomeLoadsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeLoadsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeLoadsRequestItemVH -> holder.bind(item as HomeLoadsRequestItem, _interface)
      is HomeLoadsProgressItemVH -> holder.bind(item as HomeLoadsProgressItem, _interface)
      is HomeLoadsSearchItemVH -> holder.bind(item as HomeLoadsSearchItem, _interface)
      is HomeLoadsWarningItemVH -> holder.bind(item as HomeLoadsWarningItem, _interface)
      is HomeLoadsTimeOutItemVH -> holder.bind(item as HomeLoadsTimeoutItem, _interface)
      is HomeLoadsInfoItemVH -> holder.bind(item as HomeLoadsInfoItem, _interface)
      is HomeLoadsMoreInfoItemVH -> holder.bind(item as HomeLoadsMoreInfoItem, _interface)
      is HomeLoadsFilterItemVH -> holder.bind(item as HomeLoadsFilterItem, _interface)
      is HomeLoadsMoreInfoItemVH.HomeLoadsSummaryItemVH -> holder.bind(item as HomeLoadsSummaryItem, _interface)
      is HomeLoadsAddTruckItemVH -> holder.bind(item as HomeLoadsAddTruckItem, _interface)
      is HomeLoadsTruckPriorityItemVH -> holder.bind(item as HomeLoadsTruckPriorityAccessItem, _interface)
    }
  }

  /**
   * Remove info/warning/timeout data
   */
  fun removeInfoData() {
    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeLoadsProgressItem(), AddUpdate))
      items.filter {
        it.type == Warning || it.type == Timeout || it.type == Info || it.type == MoreInfo
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
    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeLoadsProgressItem(), AddUpdate))
      items.filter {
        it.type == Count || it.type == Request || it.type == Warning || it.type == Timeout || it.type == Info || it.type == MoreInfo || it.type == Search || it.type == Filters
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