package com.delhivery.axle.ui.selectroute.fragments.routeslist

import androidx.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.axle.databinding.ViewAddRouteItemBinding
import com.delhivery.axle.databinding.ViewRouteProgressItemBinding
import com.delhivery.axle.databinding.ViewSelectRouteItemBinding
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.AddAction
import com.delhivery.axle.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.Progress

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Recycler view adapter for routes/lane preferences
 *
 **
 */
class RoutesRVAdapter(private val _interface: RoutesRVAdapterInterface) :
    BaseDataRVAdapter<RoutesRVAdapterItem<*>, ViewDataBinding,
        RoutesRVAdapterViewHolder<*, *>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ): ViewDataBinding = when (RoutesRVAdapterItemType.byTypeId(viewType)) {
    Progress -> ViewRouteProgressItemBinding.inflate(inflater, parent, false)
    AddAction -> ViewAddRouteItemBinding.inflate(inflater, parent, false)
    else -> ViewSelectRouteItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewRouteProgressItemBinding -> RoutesProgressItemVH(binding)
    is ViewAddRouteItemBinding -> AddRoutesItemVH(binding)
    else -> RoutesItemVH(binding as ViewSelectRouteItemBinding)
  }

  override fun bindVH(
    holder: RoutesRVAdapterViewHolder<*, *>,
    item: RoutesRVAdapterItem<*>
  ) {
    when (holder) {
      is RoutesItemVH -> holder.bind(item as RoutesRequestItem, _interface)
      is AddRoutesItemVH -> holder.bind(item as RoutesAddItem, _interface)
    }
  }

  fun deleteItem(position: Int) {
    notifyItemRemoved(position)
  }

}