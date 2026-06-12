package com.dfd.delfin.data.bids

import com.dfd.delfin.data.BaseKeyTypeModel

data class DmtBidSummaryProgressItemData(
    val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = DmtBidSummaryProgressItemDataKey
}

private const val DmtBidSummaryProgressItemDataKey = "progress"
