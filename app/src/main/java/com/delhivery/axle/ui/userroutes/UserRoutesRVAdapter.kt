package com.delhivery.axle.ui.userroutes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewUserRoutesItemBinding
import com.delhivery.axle.databinding.ViewUserRoutesProgressItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.userroutes.UserRoutesRVAdapterItemType.Route

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 5/1/21
 */

class UserRoutesRVAdapter(private val _interface: UserRoutesRVAdapterInterface) :
    BaseDataRVAdapter<BaseUserRouteRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (UserRoutesRVAdapterItemType.byTypeId(viewType)) {
    Route -> ViewUserRoutesItemBinding.inflate(inflater, parent, false)
    else -> ViewUserRoutesProgressItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewUserRoutesItemBinding -> UserRoutesItemVH(binding)
    else -> UserRoutesProgressItemVH(binding as ViewUserRoutesProgressItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseUserRouteRVAdapterItem<*>
  ) {
    when (holder) {
      is UserRoutesItemVH -> holder.bind(item as UserRouteItem, _interface)
      is UserRoutesProgressItemVH -> holder.bind(item as UserRouteProgressItem, _interface)
    }
  }

  /**
   *
   * Reset to empty state with progress bar
   *
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseUserRouteRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(UserRouteProgressItem(), AddUpdate))
      items.filter { it.type == Route }
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