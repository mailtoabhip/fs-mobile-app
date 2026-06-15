package com.dfd.delfin.ui.home.fragments.trips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.databinding.ViewCompletedTripItemBinding
import com.dfd.delfin.databinding.ViewHomeSearchItemBinding
import com.dfd.delfin.databinding.ViewHomeTripsHeaderItemBinding
import com.dfd.delfin.databinding.ViewHomeTripsProgressItemBinding
import com.dfd.delfin.databinding.ViewHomeTripsRequestItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewWarningItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.BaseFilterableDataRVAdapter
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsRVAdapterItemType.CompletedTrip
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Header
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Progress
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Search
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Timeout
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Warning

/**
 * RV Adapter for [HomeTripsFragment]
 */
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
    CompletedTrip -> ViewCompletedTripItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeSearchItemBinding -> HomeTripsSearchItemVH(binding)
    is ViewCompletedTripItemBinding -> HomeCompletedTripItemVH(binding)
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
      is HomeCompletedTripItemVH -> holder.bind(item as HomeCompletedTripItem, _interface)
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
      items.filter {
        it.type == TripItem || it.type == CompletedTrip || it.type == Warning ||
            it.type == Timeout || it.type == Search
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

  override fun enableFilter() {
    super.enableFilter()
    isFiltering = true
  }
}