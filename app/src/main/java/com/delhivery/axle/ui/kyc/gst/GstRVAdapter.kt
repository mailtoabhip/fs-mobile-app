package com.delhivery.axle.ui.kyc.gst

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove

/**
 * RV adapter for [GstActivity]
 */
class GstRVAdapter(private val _interface: GstRVAdapterInterface) :
    BaseDataRVAdapter<BaseGstRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (GstRVAdapterItemType.byTypeId(viewType)) {
    GstRVAdapterItemType.GstItem -> ViewGstRequestItemBinding.inflate(inflater, parent, false)
    GstRVAdapterItemType.Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    GstRVAdapterItemType.Progress -> ViewGstProgressItemBinding.inflate(inflater, parent, false)
    GstRVAdapterItemType.Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewGstRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
  is ViewGstRequestItemBinding -> GstDataItemVH(binding)
    is ViewGstProgressItemBinding -> GstProgressItemVH(binding)
    is ViewWarningItemBinding -> GstWarningItemVH(binding)
    is ViewTimeOutItemBinding -> GstTimeOutItemVH(binding)
   else -> GstDataItemVH(binding as ViewGstRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseGstRVAdapterItem<*>
  ) {
    when (holder) {
      is GstDataItemVH -> holder.bind(item as GstDataItem, _interface)
      is GstWarningItemVH -> holder.bind(item as GstWarningItem, _interface)
      is GstTimeOutItemVH -> holder.bind(item as GstTimeoutItem, _interface)
    }
  }


  fun refresh() {
    mutableListOf<Pair<BaseGstRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(GstProgressItem(), AddUpdate))
      items.filter {
        it.type == GstRVAdapterItemType.GstItem|| it.type == GstRVAdapterItemType.Warning ||
            it.type == GstRVAdapterItemType.Timeout
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