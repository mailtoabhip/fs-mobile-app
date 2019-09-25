package com.delhivery.axle.ui.home.activity.fuel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewFuelTripsProgressItemBinding
import com.delhivery.axle.databinding.ViewTripFuelItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.home.activity.fuel.TripsFuelRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.activity.fuel.TripsFuelRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.activity.fuel.TripsFuelRVAdapterItemType.Trip
import com.delhivery.axle.ui.home.activity.fuel.TripsFuelRVAdapterItemType.Warning

class TripsFuelRVAdapter(private val _interface: TripsFuelRVAdapterInterface) :
    BaseDataRVAdapter<BaseTripsFuelRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (TripsFuelRVAdapterItemType.byTypeId(viewType)) {
//    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
//    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    Progress -> ViewFuelTripsProgressItemBinding.inflate(inflater, parent, false)
    else -> ViewTripFuelItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewFuelTripsProgressItemBinding -> TripsFuelProgressItemVH(binding)
//    is ViewWarningItemBinding -> TransactionWarningItemVH(binding)
//    is ViewTimeOutItemBinding -> TransactionTimeOutItemVH(binding)
    else -> TripsFuelItemVH(binding as ViewTripFuelItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseTripsFuelRVAdapterItem<*>
  ) {
    when (holder) {
      is TripsFuelItemVH -> holder.bind(item as TripsFuelDataItem, _interface)
      is TripsFuelProgressItemVH -> holder.bind(item as TripsFuelProgressItem, _interface)
//      is TransactionWarningItemVH -> holder.bind(item as TransactionWarningItem, _interface)
//      is TransactionTimeOutItemVH -> holder.bind(item as TransactionTimeoutItem, _interface)
    }
  }

  /**
   *
   * Reset to empty state with progress bar
   *
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseTripsFuelRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(TripsFuelProgressItem(), AddUpdate))
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