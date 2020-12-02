package com.delhivery.axle.ui.ledger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.adapter.BaseExpandableDataRVAdapter
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Search
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Ledger
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Warning
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Timeout
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Progress

class ConsolidatedPageRVAdapter(private val _interface: ConsolidatedPageRVAdapterInterface):
BaseExpandableDataRVAdapter<BaseConsolidatedPageRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
){
    override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

    override fun getBinding(
            inflater: LayoutInflater,
            parent: ViewGroup,
            viewType: Int
    ) = when (ConsolidatedPageRVAdapterItemType.byTypeId(viewType)){
        Search -> ViewHomeSearchItemBinding.inflate(inflater,parent,false)
        Ledger -> ViewConsolidatedPageLedgerItemBinding.inflate(inflater, parent, false)
        Warning -> ViewWarningItemBinding.inflate(inflater, parent,false)
        Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
        Progress -> ViewProgressItemBinding.inflate(inflater,parent,false)
        else -> ViewProgressItemBinding.inflate(inflater, parent, false)
    }

    override fun onGroupExpand(position: Int, ledgers: ConsolidatedLedgerItemData) {
//        items.addAll(position + 1, mutableListOf<BaseConsolidatedPageRVAdapterItem<*>>().apply {
//            for (data in ledgers) {
//                add(ConsolidatedPageLedgerItem(data))
//            }
//        })
        notifyItemChanged(position)
//        notifyItemRangeInserted(position + 1, ledgers.size)
//        notifyItemRangeChanged(position + ledgers.size, items.size - 1)
    }

    override fun onGroupCollapse(position: Int, ledgers: ConsolidatedLedgerItemData) {
//        for (data in ledgers) {
//            items.removeAt(position + 1)
//        }
        notifyItemChanged(position)
//        notifyItemRangeRemoved(position + 1, ledgers.size)
//        notifyItemRangeChanged(position + 1, items.size - 1)

    }

    override fun createVH(binding: ViewDataBinding) = when (binding) {
        is ViewHomeSearchItemBinding -> ConsolidatedPageSearchItemVH(binding)
        is ViewConsolidatedPageLedgerItemBinding -> ConsolidatedPageLedgerItemVH(binding)
        is ViewWarningItemBinding -> ConsolidatedPageWarningItemVH(binding)
        is ViewTimeOutItemBinding -> ConsolidatedPageTimeOutItemVH(binding)
        is ViewProgressItemBinding -> ConsolidatedPageProgressItemVH(binding)
        else -> ConsolidatedPageLedgerItemVH(binding as ViewConsolidatedPageLedgerItemBinding)

    }

    override fun bindVH(holder: BaseViewHolder<*>, item: BaseConsolidatedPageRVAdapterItem<*>) {
        when (holder){
            is ConsolidatedPageSearchItemVH -> holder.bind(item as ConsolidatedPageSearchItem, _interface)
            is ConsolidatedPageLedgerItemVH -> holder.bind(item as ConsolidatedPageLedgerItem, _interface)
            is ConsolidatedPageWarningItemVH -> holder.bind(item as ConsolidatedPageWarningItem, _interface)
            is ConsolidatedPageTimeOutItemVH -> holder.bind(item as ConsolidatedPageTimeoutItem, _interface)
            is ConsolidatedPageProgressItemVH -> holder.bind(item as ConsolidatedPageProgressItem, _interface)
        }
    }
}