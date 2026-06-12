package com.dfd.delfin.ui.ledger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.data.ledger.ConsolidatedLedgerItemData
import com.dfd.delfin.databinding.*
import com.dfd.delfin.ui.base.adapter.BaseExpandableDataRVAdapter
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Search
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Ledger
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Warning
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Timeout
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Progress

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
        notifyItemChanged(position)
    }

    override fun onGroupCollapse(position: Int, ledgers: ConsolidatedLedgerItemData) {
        notifyItemChanged(position)
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

    fun resetStaticData() {
        mutableListOf<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                  //add(Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()), DataRVAdapterOperationType.AddUpdate))
            items.filter { it.type == Ledger || it.type == Warning || it.type == Timeout }
                    .map { Pair(it, DataRVAdapterOperationType.Remove) }
                    .let {
                        addAll(it)
                    }
        }
                .let {
                    operation(it)
                }
    }
}