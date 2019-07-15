package com.delhivery.orion.ui.bids

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeTripsProgressItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsRequestItemBinding
import com.delhivery.orion.databinding.ViewTimeOutItemBinding
import com.delhivery.orion.databinding.ViewWarningItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsItemVH
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsProgressItemVH
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterInterface
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Progress
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Timeout
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Warning
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsTimeOutItemVH
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsTimeoutItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsWarningItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsWarningItemVH
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsWarningItem_NoLoads
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsWarningItem_TimeOut

class TripsRVAdapter(private val _interface: HomeTripsRVAdapterInterface) :
    BaseFilterableDataRVAdapter<BaseHomeTripsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeTripsRVAdapterItemType.byTypeId(viewType)) {
    Progress -> ViewHomeTripsProgressItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeTripsProgressItemBinding -> HomeTripsProgressItemVH(binding)
    is ViewWarningItemBinding -> HomeTripsWarningItemVH(binding)
    is ViewTimeOutItemBinding -> HomeTripsTimeOutItemVH(binding)
    else -> HomeTripsItemVH(binding as ViewHomeTripsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeTripsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeTripsProgressItemVH -> holder.bind(item as HomeTripsProgressItem, _interface)
      is HomeTripsItemVH -> holder.bind(item as HomeTripsItem, _interface)
      is HomeTripsWarningItemVH -> holder.bind(item as HomeTripsWarningItem, _interface)
      is HomeTripsTimeOutItemVH -> holder.bind(item as HomeTripsTimeoutItem, _interface)
    }
  }

  /**
   * Reset to empty state with search bar
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeTripsProgressItem(), AddUpdate))
      add(Pair(HomeTripsWarningItem_NoLoads, Remove))
      add(Pair(HomeTripsWarningItem_TimeOut, Remove))
      items.filter { it.type == TripItem }
          .map { Pair(it, Remove) }
          .let {
            addAll(it)
          }
    }
        .let {
          operation(it)
        }
  }

  override fun filterList(query: String) = items.filter { it.data.filter(query) }
}