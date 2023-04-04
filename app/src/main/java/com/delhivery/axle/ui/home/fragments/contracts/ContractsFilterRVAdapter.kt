package com.delhivery.axle.ui.home.fragments.contracts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewFilterInfoItemBinding
import com.delhivery.axle.databinding.ViewFilterToggleItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.FilterToggle

/**
 * Adapter for contract filter recycler view
 */
class ContractsFilterRVAdapter(private val _interface:HomeContractsRVAdapterInterface) :
  BaseDataRVAdapter<BaseHomeContractsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
    _interface
  ) {

  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when(HomeContractsRVAdapterItemType.byTypeId(viewType)){
      FilterToggle -> ViewFilterToggleItemBinding.inflate(inflater,parent,false)
      else -> ViewFilterInfoItemBinding.inflate(inflater,parent,false)
    }

  override fun bindVH(holder: BaseViewHolder<*>, item: BaseHomeContractsRVAdapterItem<*>) {
      when(holder){
        is FilterToggleItemVH -> holder.bind(item as FilterToggleItem,_interface)
        is FilterInfoItemVH -> holder.bind(item as FilterInfoItem,_interface)
      }
  }

  override fun createVH(binding: ViewDataBinding) = when(binding) {
      is ViewFilterToggleItemBinding -> FilterToggleItemVH(binding)
      else -> FilterInfoItemVH(binding as ViewFilterInfoItemBinding)
  }
}