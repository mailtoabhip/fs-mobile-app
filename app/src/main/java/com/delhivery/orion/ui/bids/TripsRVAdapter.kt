package com.delhivery.orion.ui.bids

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeTripsDetailsItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsProgressItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.orion.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsItemVH
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsProgressItemVH
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterInterface
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Progress

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
    else -> ViewHomeTripsDetailsItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeTripsProgressItemBinding -> HomeTripsProgressItemVH(binding)
    else -> HomeTripsItemVH(binding as ViewHomeTripsDetailsItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeTripsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeTripsProgressItemVH -> holder.bind(item as HomeTripsProgressItem, _interface)
      is HomeTripsItemVH -> holder.bind(item as HomeTripsItem, _interface)
    }
  }

  override fun filterList(query: String) = items.filter { it.data.filter(query) }
}