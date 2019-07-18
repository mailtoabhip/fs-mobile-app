package com.delhivery.axle.ui.home.fragments.bids

import androidx.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.axle.data.home.bids.HomeBidsHeaderItemData
import com.delhivery.axle.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Header
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Warning

class HomeBidsRVAdapter(private val _interface: HomeBidsRVAdapterInterface) :
    BaseFilterableDataRVAdapter<BaseHomeBidsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeBidsRVAdapterItemType.byTypeId(viewType)) {
    Header -> ViewHomeBidsHeaderItemBinding.inflate(inflater, parent, false)
    Search -> ViewHomeSearchItemBinding.inflate(inflater, parent, false)
    Request -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeBidsProgressItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeBidsHeaderItemBinding -> HomeBidsHeaderItemVH(binding)
    is ViewHomeSearchItemBinding -> HomeBidsSearchItemVH(binding)
    is ViewHomeBidsRequestItemBinding -> HomeBidsRequestItemVH(binding)
    is ViewWarningItemBinding -> HomeBidsWarningItemVH(binding)
    is ViewTimeOutItemBinding -> HomeBidsTimeOutItemVH(binding)
    is ViewHomeBidsProgressItemBinding -> HomeBidsProgressItemVH(binding)
    else -> HomeBidsRequestItemVH(binding as ViewHomeBidsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeBidsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeBidsHeaderItemVH -> holder.bind(item as HomeBidsHeaderItem, _interface)
      is HomeBidsSearchItemVH -> holder.bind(item as HomeBidsSearchItem, _interface)
      is HomeBidsRequestItemVH -> holder.bind(item as HomeBidsRequestItem, _interface)
      is HomeBidsWarningItemVH -> holder.bind(item as HomeBidsWarningItem, _interface)
      is HomeBidsTimeOutItemVH -> holder.bind(item as HomeBidsTimeoutItem, _interface)
    }
  }

  override fun filterList(query: String) =
    items.filter { it.type == Search || it.data.filter(query) }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeBidsHeaderItem(HomeBidsHeaderItemData()), Update))
      add(Pair(HomeBidsWarningItem_NoBids, Remove))
      add(Pair(HomeBidsWarningItem_TimeOut, Remove))
      add(Pair(HomeBidsProgressItem(), AddUpdate))
      items.filter { it.type == Request }
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