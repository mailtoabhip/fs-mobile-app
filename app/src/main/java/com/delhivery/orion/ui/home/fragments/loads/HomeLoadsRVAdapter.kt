package com.delhivery.orion.ui.home.fragments.loads

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Progress
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request

class HomeLoadsRVAdapter(private val _interface: HomeLoadsRVAdapterInterface) :
    BaseDataRVAdapter<BaseHomeLoadsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeLoadsRVAdapterItemType.byTypeId(
      viewType
  )) {
    Request -> ViewHomeLoadsRequestItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeLoadsProgressItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeLoadsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeLoadsRequestItemBinding -> HomeLoadsRequestItemVH(
        binding
    )
    is ViewHomeLoadsProgressItemBinding -> HomeLoadsProgressItemVH(
        binding
    )
    else -> HomeLoadsRequestItemVH(
        binding as ViewHomeLoadsRequestItemBinding
    )
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeLoadsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeLoadsRequestItemVH -> holder.bind(item as HomeLoadsRequestItem, _interface)
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
    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
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