package com.delhivery.orion.ui.home.fragments.bids

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsWarningItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Header
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Progress
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Search
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Warning

class HomeBidsRVAdapter(private val _interface: HomeBidsRVAdapterInterface) :
    BaseDataRVAdapter<BaseHomeBidsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeBidsRVAdapterItemType.byTypeId(viewType)) {
    Header -> ViewHomeBidsHeaderItemBinding.inflate(inflater, parent, false)
    Search -> ViewHomeBidsSearchItemBinding.inflate(
        inflater, parent,
        false
    )
    Request -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
    Warning -> ViewHomeBidsWarningItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeBidsProgressItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeBidsHeaderItemBinding -> HomeBidsHeaderItemVH(binding)
    is ViewHomeBidsSearchItemBinding -> HomeBidsSearchItemVH(binding)
    is ViewHomeBidsRequestItemBinding -> HomeBidsRequestItemVH(binding)
    is ViewHomeBidsWarningItemBinding -> HomeBidsWarningItemVH(binding)
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
    }
  }

  /**
   * Remove all transactions
   */
  fun removeAllTransactions() {
    items.filter { it.type == Request }
        .map { Pair(it, Remove) }
        .let {
          operation(it)
        }
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeBidsHeaderItem(), Update))
      add(Pair(HomeBidsSearchItem(), Update))
      add(Pair(HomeBidsWarningItem_SelectRoutes, Remove))
      add(Pair(HomeBidsWarningItem_EditRoutePrefs, Remove))
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
}