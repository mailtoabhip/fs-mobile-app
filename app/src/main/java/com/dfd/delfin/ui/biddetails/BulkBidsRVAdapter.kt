package com.dfd.delfin.ui.biddetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.databinding.*
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.BaseSummaryDataRVAdapter

class BulkBidsRVAdapter(private val _interface : BulkBidsRVAdapterInterface):
    BaseSummaryDataRVAdapter<BaseBulkBidSummaryRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ){

    override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = when(BulkBidsSummaryRVAdapterItemType.byTypeId(viewType)){
       BulkBidsSummaryRVAdapterItemType.Summary -> ViewBidDetailItemBinding.inflate(inflater, parent, false)
        BulkBidsSummaryRVAdapterItemType.Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
        else -> ViewProgressItemBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ViewDataBinding) = when(binding){
        is ViewBidDetailItemBinding -> BulkBidsSummaryItemVH(binding)
        is ViewTimeOutItemBinding -> BulkBidSummaryTimeOutItemVH(binding)
        else -> BulkBidSummaryProgressItemVH(binding as ViewProgressItemBinding)
    }

    override fun bindVH(holder: BaseViewHolder<*>, item: BaseBulkBidSummaryRVAdapterItem<*>) {
        when(holder){
            is BulkBidsSummaryItemVH -> holder.bind(item as BulkBidSummaryItem, _interface)
            is BulkBidSummaryProgressItemVH -> holder.bind(item as BulkBidSummaryProgressItem, _interface)
            is BulkBidSummaryTimeOutItemVH -> holder.bind(item as BulkBidTimeoutItem, _interface)
        }

    }
}