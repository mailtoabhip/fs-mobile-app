package com.dfd.delfin.ui.home.activity.fuel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.databinding.ViewActiveTripItemBinding
import com.dfd.delfin.databinding.ViewActiveTripsProgressItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewWarningItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Progress
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Timeout
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Trip
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Warning

/**
 * RV Adapter for [ActiveTripsActivity]
 */
class ActiveTripsRVAdapter(private val _interface: ActiveTripsRVAdapterInterface) :
    BaseDataRVAdapter<BaseActiveTripsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (ActiveTripsRVAdapterItemType.byTypeId(viewType)) {
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    Progress -> ViewActiveTripsProgressItemBinding.inflate(inflater, parent, false)
    else -> ViewActiveTripItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewActiveTripsProgressItemBinding -> ActiveTripsProgressItemVH(binding)
    is ViewWarningItemBinding -> ActiveTripsWarningItemVH(binding)
    is ViewTimeOutItemBinding -> ActivetripsTimeOutItemVH(binding)
    else -> ActiveTripsItemVH(binding as ViewActiveTripItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseActiveTripsRVAdapterItem<*>
  ) {
    when (holder) {
      is ActiveTripsItemVH -> holder.bind(item as ActiveTripFuelDataItem, _interface)
      is ActiveTripsProgressItemVH -> holder.bind(item as ActiveTripProgressItem, _interface)
      is ActiveTripsWarningItemVH -> holder.bind(item as ActiveTripWarningItem, _interface)
      is ActivetripsTimeOutItemVH -> holder.bind(item as ActiveTripTimeoutItem, _interface)
    }
  }

  /**
   *
   * Reset to empty state with progress bar
   *
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseActiveTripsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(ActiveTripProgressItem(), AddUpdate))
      items.filter { it.type == Trip || it.type == Warning || it.type == Timeout }
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