package com.delhivery.axle.ui.base.adapter

import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.ConsolidatedMonthItemData
import com.delhivery.axle.ui.base.BaseViewHolder

abstract class BaseExpandableDataRVAdapter<
        D : BaseKeyTypeModel<out Any>,
        B: ViewDataBinding,
        VH: BaseViewHolder<*>>(clickListener: BaseConsolidatedPageDataRVAdapter.ItemClickListener<D>):
BaseConsolidatedPageFilterableDataRVAdapter<D, B, VH>(clickListener){
    fun toggle(
            position: Int,
            data: ConsolidatedMonthItemData
    ){
        if(data.expanded){
            data.expanded = false
            onGroupCollapse(position, data.ledgerData)
        }else{
            data.expanded = true
            onGroupExpand(position,data.ledgerData)
        }
    }

    abstract fun onGroupExpand(
            position: Int,
            ledgers: List<ConsolidatedLedgerItemData>
    )

    abstract fun onGroupCollapse(
            position: Int,
            ledgers: List<ConsolidatedLedgerItemData>
    )
}