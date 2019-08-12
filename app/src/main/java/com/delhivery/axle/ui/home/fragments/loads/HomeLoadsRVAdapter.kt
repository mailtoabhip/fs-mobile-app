package com.delhivery.axle.ui.home.fragments.loads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewHomeLoadsInfoItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsSearchItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Info
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Warning

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
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Info -> ViewHomeLoadsInfoItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeLoadsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeLoadsSearchItemBinding -> HomeLoadsSearchItemVH(binding)
    is ViewHomeLoadsProgressItemBinding -> HomeLoadsProgressItemVH(binding)
    is ViewWarningItemBinding -> HomeLoadsWarningItemVH(binding)
    is ViewHomeLoadsInfoItemBinding -> HomeLoadsInfoItemVH(binding)
    is ViewTimeOutItemBinding -> HomeLoadsTimeOutItemVH(binding)
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
      is HomeLoadsTimeOutItemVH -> holder.bind(item as HomeLoadsTimeoutItem, _interface)
      is HomeLoadsInfoItemVH -> holder.bind(item as HomeLoadsInfoItem, _interface)
    }
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomeLoadsProgressItem(), AddUpdate))
      items.filter {
        it.type == Request || it.type == Warning ||
            it.type == Timeout || it.type == Info
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