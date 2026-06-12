package com.dfd.delfin.ui.searchongoingtrip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.databinding.ViewHomeBidsProgressItemBinding
import com.dfd.delfin.databinding.ViewHomeTripsRequestItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewWarningItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.dfd.delfin.ui.searchongoingtrip.SearchOngoingTripRVAdapterItemType.Progress
import com.dfd.delfin.ui.searchongoingtrip.SearchOngoingTripRVAdapterItemType.Timeout
import com.dfd.delfin.ui.searchongoingtrip.SearchOngoingTripRVAdapterItemType.TripItem
import com.dfd.delfin.ui.searchongoingtrip.SearchOngoingTripRVAdapterItemType.Warning

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 13/5/21
 */

class SearchOngoingTripRVAdapter(private val _interface: SearchOngoingTripRVAdapterInterface) :
    BaseDataRVAdapter<BaseSearchOngoingTripRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (SearchOngoingTripRVAdapterItemType.byTypeId(viewType)) {
    TripItem -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeBidsProgressItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeTripsRequestItemBinding -> SearchDataItemVH(binding)
    is ViewWarningItemBinding -> SearchWarningItemVH(binding)
    is ViewTimeOutItemBinding -> SearchTimeOutItemVH(binding)
    is ViewHomeBidsProgressItemBinding -> SearchProgressItemVH(binding)
    else -> SearchDataItemVH(binding as ViewHomeTripsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseSearchOngoingTripRVAdapterItem<*>
  ) {
    when (holder) {
      is SearchDataItemVH -> holder.bind(item as SearchDataItem, _interface)
      is SearchWarningItemVH -> holder.bind(item as SearchWarningItem, _interface)
      is SearchTimeOutItemVH -> holder.bind(item as SearchTimeoutItem, _interface)
    }
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseSearchOngoingTripRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      items.filter {
        it.type == TripItem || it.type == Warning || it.type == Timeout
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