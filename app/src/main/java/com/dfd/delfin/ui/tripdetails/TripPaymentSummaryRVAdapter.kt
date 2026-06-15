package com.dfd.delfin.ui.tripdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.data.tripdetail.TripPaymentSummaryDetailItemData
import com.dfd.delfin.databinding.ViewProgressItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewTripPaymentSummaryDetailItemBinding
import com.dfd.delfin.databinding.ViewTripPaymentSummaryItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.BaseSummaryExpandableDataRVAdapter
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.tripdetails.TripPaymentSummaryRVAdapterItemType.Summary
import com.dfd.delfin.ui.tripdetails.TripPaymentSummaryRVAdapterItemType.Detail
import com.dfd.delfin.ui.tripdetails.TripPaymentSummaryRVAdapterItemType.Timeout

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

class TripPaymentSummaryRVAdapter(private val _interface: TripPaymentSummaryRVAdapterInterface):
    BaseSummaryExpandableDataRVAdapter<BaseTripPaymentSummaryRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>> (
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when(TripPaymentSummaryRVAdapterItemType.byTypeId(viewType)) {
    Summary -> ViewTripPaymentSummaryItemBinding.inflate(inflater, parent, false)
    Detail -> ViewTripPaymentSummaryDetailItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewProgressItemBinding.inflate(inflater, parent, false)
  }

  override fun onGroupExpand(
    position: Int,
    summary: List<TripPaymentSummaryDetailItemData>
  ) {
    items.addAll(position + 1, mutableListOf<BaseTripPaymentSummaryRVAdapterItem<*>>().apply {
      for (detailSummary in summary) {
        add(TripSummaryDetailItem(detailSummary))
      }
    })
    notifyItemChanged(position)
    notifyItemRangeInserted(position + 1, summary.size)
    notifyItemRangeChanged(position + summary.size, items.size - 1)
  }

  override fun onGroupCollapse(
    position: Int,
    summary: List<TripPaymentSummaryDetailItemData>
  ) {
    for (detailSummary in summary) {
      items.removeAt(position + 1)
    }
    notifyItemChanged(position)
    notifyItemRangeRemoved(position + 1, summary.size)
    notifyItemRangeChanged(position + 1, items.size - 1)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewTripPaymentSummaryItemBinding -> TripPaymentSummaryItemVH(binding)
    is ViewTripPaymentSummaryDetailItemBinding -> TripPaymentDetailSummaryItemVH(binding)
    is ViewTimeOutItemBinding -> TripPaymentSummaryTimeOutItemVH(binding)
    else -> TripPaymentSummaryProgressItemVH(binding as ViewProgressItemBinding)
  }

  override fun bindVH(holder: BaseViewHolder<*>, item: BaseTripPaymentSummaryRVAdapterItem<*>) {
    when (holder) {
      is TripPaymentSummaryItemVH -> holder.bind(item as TripSummaryItem, _interface)
      is TripPaymentDetailSummaryItemVH -> holder.bind(item as TripSummaryDetailItem, _interface)
      is TripPaymentSummaryTimeOutItemVH -> holder.bind(item as TripSummaryTimeoutItem, _interface)
      is TripPaymentSummaryProgressItemVH -> holder.bind(item as TripSummaryProgressItem, _interface)
    }
  }

  fun resetStaticData() {
    mutableListOf<Pair<BaseTripPaymentSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      items.filter { it.type == Summary || it.type == Detail || it.type == Timeout }
          .map { Pair(it, DataRVAdapterOperationType.Remove) }
          .let {
            addAll(it)
          }
    }.let {
      operation(it)
    }
  }

}