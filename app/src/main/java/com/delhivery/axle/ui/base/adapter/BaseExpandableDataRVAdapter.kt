package com.delhivery.axle.ui.base.adapter

import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.ui.base.BaseViewHolder

abstract class BaseExpandableDataRVAdapter<
        D : BaseKeyTypeModel<out Any>,
        B: ViewDataBinding,
        VH: BaseViewHolder<*>>(clickListener: BaseConsolidatedPageDataRVAdapter.ItemClickListener<D>):
BaseConsolidatedPageFilterableDataRVAdapter<D, B, VH>(clickListener){
    fun toggle(
            position: Int,
            data: ConsolidatedLedgerItemData
    ){
        if(data.expanded){
            data.expanded = false
            onGroupCollapse(position,data)
        }else{
            data.expanded = true
            onGroupExpand(position,data)
        }
    }

    abstract fun onGroupExpand(
            position: Int,
            ledgers: ConsolidatedLedgerItemData
    )

    abstract fun onGroupCollapse(
            position: Int,
            ledgers: ConsolidatedLedgerItemData
    )
}