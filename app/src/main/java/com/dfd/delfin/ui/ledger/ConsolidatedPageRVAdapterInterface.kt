package com.dfd.delfin.ui.ledger

import com.dfd.delfin.ui.base.adapter.BaseConsolidatedPageDataRVAdapter.ItemClickListener


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