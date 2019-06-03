package com.delhivery.orion.ui.home.fragments.trips

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeTripsDetailsItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsHeaderItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsProgressItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsSearchItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Header
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Progress
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Search
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem

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
    Search -> ViewHomeTripsSearchItemBinding.inflate(inflater, parent, false)
    TripItem -> ViewHomeTripsDetailsItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeTripsDetailsItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeTripsSearchItemBinding -> HomeTripsSearchItemVH(binding)
    is ViewHomeTripsProgressItemBinding -> HomeTripsProgressItemVH(binding)
    is ViewHomeTripsHeaderItemBinding -> HomeTripsHeaderItemVH(binding)
    else -> HomeTripsItemVH(binding as ViewHomeTripsDetailsItemBinding)
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
    }
  }

  override fun filterList(query: String) =
    items.filter { it.type == Search || it.data.filter(query) }

  /**
   * Reset to empty state with search bar
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomeTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeTripsProgressItem(), AddUpdate))
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

  override fun enableFilter() {
    super.enableFilter()
    isFiltering = true
  }
}