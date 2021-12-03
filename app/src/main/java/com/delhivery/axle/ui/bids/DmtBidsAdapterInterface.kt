package com.delhivery.axle.ui.bids

import com.delhivery.axle.ui.base.adapter.BaseSummaryDataRVAdapter

interface DmtBidsAdapterInterface: BaseSummaryDataRVAdapter.ItemClickListener<BaseDmtBidSummaryRVAdapterItem<*>> {
    override fun onItemClicked(item: BaseDmtBidSummaryRVAdapterItem<*>, position: Int) {}


    fun handleAction(
        actionId: String,
        position: Int,
        item: BaseDmtBidSummaryRVAdapterItem<*>
    )
}