package com.dfd.delfin.ui.bids

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.databinding.ViewBidCreateEditItemBinding
import com.dfd.delfin.databinding.ViewProgressItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.ui.base.adapter.BaseSummaryDataRVAdapter
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.biddetails.DmtBidsAdapterInterface

class DmtBidsRVAdapter(private val _interface : DmtBidsAdapterInterface):
    BaseSummaryDataRVAdapter<BaseDmtBidSummaryRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(_interface) {

    override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = when(DmtBidsSummaryRVAdapterItemType.byTypeId(viewType)){
            DmtBidsSummaryRVAdapterItemType.Summary -> ViewBidCreateEditItemBinding.inflate(inflater, parent, false)
            DmtBidsSummaryRVAdapterItemType.Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
            else -> ViewProgressItemBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ViewDataBinding)= when(binding){
        is ViewBidCreateEditItemBinding -> DmtBidsSummaryItemVH(binding)
        is ViewTimeOutItemBinding -> DmtBidSummaryTimeOutItemVH(binding)
        else -> DmtBidSummaryProgressItemVH(binding as ViewProgressItemBinding)
    }

    override fun bindVH(holder: BaseViewHolder<*>, item: BaseDmtBidSummaryRVAdapterItem<*>) {

        when(holder){
            is DmtBidsSummaryItemVH -> holder.bind(item as DmtBidSummaryItem, _interface)
            is DmtBidSummaryProgressItemVH -> holder.bind(item as DmtBidSummaryProgressItem, _interface)
            is DmtBidSummaryTimeOutItemVH -> holder.bind(item as BulkBidTimeoutItem, _interface)
        }
    }


}