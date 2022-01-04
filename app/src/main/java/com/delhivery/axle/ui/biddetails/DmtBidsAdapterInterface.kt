package com.delhivery.axle.ui.biddetails

import com.delhivery.axle.ui.base.adapter.BaseSummaryDataRVAdapter
import com.delhivery.axle.ui.bids.BaseDmtBidSummaryRVAdapterItem

interface DmtBidsAdapterInterface: BaseSummaryDataRVAdapter.ItemClickListener<BaseDmtBidSummaryRVAdapterItem<*>> {
    override fun onItemClicked(item: BaseDmtBidSummaryRVAdapterItem<*>, position: Int) {}


    fun handleAction(
        actionId: String,
        position: Int,
        item: BaseDmtBidSummaryRVAdapterItem<*>
    )

    fun itemCapacity(capacity:Double)


}