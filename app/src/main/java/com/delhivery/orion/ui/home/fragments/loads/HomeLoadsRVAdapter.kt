package com.delhivery.orion.ui.home.fragments.loads

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeLoadsInfoItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsSearchItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsWarningItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Info
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Progress
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Search
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Warning

class HomeLoadsRVAdapter(private val _interface: HomeLoadsRVAdapterInterface) :
    BaseDataRVAdapter<BaseHomeLoadsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeLoadsRVAdapterItemType.byTypeId(viewType)) {
    Search -> ViewHomeLoadsSearchItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeLoadsProgressItemBinding.inflate(inflater, parent, false)
    Warning -> ViewHomeLoadsWarningItemBinding.inflate(inflater, parent, false)
    Info -> ViewHomeLoadsInfoItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeLoadsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeLoadsSearchItemBinding -> HomeLoadsSearchItemVH(binding)
    is ViewHomeLoadsProgressItemBinding -> HomeLoadsProgressItemVH(binding)
    is ViewHomeLoadsWarningItemBinding -> HomeLoadsWarningItemVH(binding)
    is ViewHomeLoadsInfoItemBinding -> HomeLoadsInfoItemVH(binding)
    else -> HomeLoadsRequestItemVH(binding as ViewHomeLoadsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeLoadsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeLoadsRequestItemVH -> holder.bind(item as HomeLoadsRequestItem, _interface)
      is HomeLoadsProgressItemVH -> holder.bind(item as HomeLoadsProgressItem, _interface)
      is HomeLoadsSearchItemVH -> holder.bind(item as HomeLoadsSearchItem, _interface)
      is HomeLoadsWarningItemVH -> holder.bind(item as HomeLoadsWarningItem, _interface)
      is HomeLoadsInfoItemVH -> holder.bind(item as HomeLoadsInfoItem, _interface)
    }
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeLoadsWarningItem_NoLoads, Remove))
      add(Pair(HomeLoadsInfoItem(), Remove))
      add(Pair(HomeLoadsProgressItem(), AddUpdate))
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