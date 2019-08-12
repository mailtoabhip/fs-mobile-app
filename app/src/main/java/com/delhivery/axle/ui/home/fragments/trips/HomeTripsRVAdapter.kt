package com.delhivery.axle.ui.home.fragments.trips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewHomeTripsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeTripsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeTripsRequestItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Header
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Warning

class HomeTripsRVAdapter(private val _interface: HomeTripsRVAdapterInterface) :
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
    Header -> ViewHomeTripsHeaderItemBinding.inflate(inflater, parent, false)
    Search -> ViewHomeSearchItemBinding.inflate(inflater, parent, false)
    TripItem -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeSearchItemBinding -> HomeTripsSearchItemVH(binding)
    is ViewHomeTripsProgressItemBinding -> HomeTripsProgressItemVH(binding)
    is ViewHomeTripsHeaderItemBinding -> HomeTripsHeaderItemVH(binding)
    is ViewWarningItemBinding -> HomeTripsWarningItemVH(binding)
    is ViewTimeOutItemBinding -> HomeTripsTimeOutItemVH(binding)
    else -> HomeTripsItemVH(binding as ViewHomeTripsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeTripsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeTripsSearchItemVH -> holder.bind(item as HomeTripsSearchItem, _interface)
      is HomeTripsItemVH -> holder.bind(item as HomeTripsItem, _interface)
      is HomeTripsProgressItemVH -> holder.bind(item as HomeTripsProgressItem, _interface)
      is HomeTripsHeaderItemVH -> holder.bind(item as HomeTripsHeaderItem, _interface)
      is HomeTripsWarningItemVH -> holder.bind(item as HomeTripsWarningItem, _interface)
      is HomeTripsTimeOutItemVH -> holder.bind(item as HomeTripsTimeoutItem, _interface)
    }
  }

  override fun filterList(query: String) =
    items.filter { it.type == Search || it.data.filter(query) }

  /**
   * Reset to empty state with search bar
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeTripsHeaderItem(), AddUpdate))
      add(Pair(HomeTripsProgressItem(), AddUpdate))
      items.filter { it.type == TripItem || it.type == Warning || it.type == Timeout }
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
    super.enableFilter()
    isFiltering = true
  }
}