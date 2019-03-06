package com.delhivery.orion.ui.home.fragments.bids

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Header
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Search

class HomeBidsRVAdapter(clickInterface: ItemClickListener<BaseHomeBidsRVAdapterItem<*>>) :
    BaseDataRVAdapter<BaseHomeBidsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        clickInterface
    ) {

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomeBidsRVAdapterItemType.byTypeId(viewType)) {
    Header -> ViewHomeBidsHeaderItemBinding.inflate(inflater, parent, false)
    Search -> ViewHomeBidsSearchItemBinding.inflate(inflater, parent, false)
    //todo - this shouldnt be here
    else -> ViewHomeBidsHeaderItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeBidsHeaderItemBinding -> HomeBidsHeaderItemVH(binding)
    is ViewHomeBidsSearchItemBinding -> HomeBidsSearchItemVH(binding)
    //todo - this shouldnt be here
    else -> HomeBidsHeaderItemVH(binding as ViewHomeBidsHeaderItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomeBidsRVAdapterItem<*>
  ) {
    when (holder) {
      is HomeBidsHeaderItemVH -> holder.bind(item as HomeBidsHeaderItem)
      is HomeBidsSearchItemVH -> holder.bind(item as HomeBidsSearchItem)
    }
  }
}