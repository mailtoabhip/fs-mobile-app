package com.dfd.delfin.ui.biddetails

import com.dfd.delfin.ui.base.adapter.BaseSummaryDataRVAdapter

interface BulkBidsRVAdapterInterface : BaseSummaryDataRVAdapter.ItemClickListener<BaseBulkBidSummaryRVAdapterItem<*>> {

    override fun onItemClicked(item: BaseBulkBidSummaryRVAdapterItem<*>, position: Int) {}

    /**
     * Handle specific action
     */
    fun handleAction(
        actionId: String,
        position: Int,
        item: BaseBulkBidSummaryRVAdapterItem<*>
    )

}