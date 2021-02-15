package com.delhivery.axle.ui.ledger

import com.delhivery.axle.ui.base.adapter.BaseConsolidatedPageDataRVAdapter.ItemClickListener


interface ConsolidatedPageRVAdapterInterface: ItemClickListener<BaseConsolidatedPageRVAdapterItem<*>> {

    override fun onItemClicked(item: BaseConsolidatedPageRVAdapterItem<*>, position: Int) {}

    /**
     * Handle specific action
     */
    fun handleAction(
            actionId: String,
            position: Int,
            item: BaseConsolidatedPageRVAdapterItem<*>
    )
}