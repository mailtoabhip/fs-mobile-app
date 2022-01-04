package com.delhivery.axle.data.bids

import com.delhivery.axle.data.BaseKeyTypeModel

data class DmtBidSummaryProgressItemData(
    val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = DmtBidSummaryProgressItemDataKey
}

private const val DmtBidSummaryProgressItemDataKey = "progress"
