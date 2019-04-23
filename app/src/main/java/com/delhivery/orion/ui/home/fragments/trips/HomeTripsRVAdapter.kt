package com.delhivery.orion.ui.home.fragments.trips

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeTripsDetailsItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsSearchItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Search
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem

class HomeTripsRVAdapter(private val _interface: ItemClickListener<BaseHomeTripsRVAdapterItem<*>>) :
    BaseFilterableDataRVAdapter<BaseHomeTripsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeTripsRVAdapterItemType.byTypeId(viewType)) {
    Search -> ViewHomeTripsSearchItemBinding.inflate(inflater, parent, false)
    TripItem -> ViewHomeTripsDetailsItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeTripsDetailsItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeTripsSearchItemBinding -> HomeTripsSearchItemVH(binding)
    else -> HomeTripsItemVH(binding as ViewHomeTripsDetailsItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeTripsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeTripsSearchItemVH -> holder.bind(item as HomeTripsSearchItem)
      is HomeTripsItemVH -> holder.bind(item as HomeTripsItem)
    }
  }

  override fun filterList(query: String) = items.filter { it.data.filter(query) }

  /**
   * Reset to empty state with search bar
   */
  fun reset() {
    items.filter { it.type == TripItem }
        .map { Pair(it, Remove) }
        .let {
          operation(it)
        }
  }
}