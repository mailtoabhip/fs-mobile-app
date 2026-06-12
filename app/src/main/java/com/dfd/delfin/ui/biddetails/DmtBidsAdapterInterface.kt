package com.dfd.delfin.ui.biddetails

import com.dfd.delfin.ui.base.adapter.BaseSummaryDataRVAdapter
import com.dfd.delfin.ui.bids.BaseDmtBidSummaryRVAdapterItem

interface DmtBidsAdapterInterface: BaseSummaryDataRVAdapter.ItemClickListener<BaseDmtBidSummaryRVAdapterItem<*>> {
    override fun onItemClicked(item: BaseDmtBidSummaryRVAdapterItem<*>, position: Int) {}

    fun handleAction(
        actionId: String,
        position: Int,
        item: BaseDmtBidSummaryRVAdapterItem<*>
    )

    fun itemCapacity(capacity:Double)
}