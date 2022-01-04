package com.delhivery.axle.data.biddetail

import com.delhivery.axle.data.BaseKeyTypeModel


data class BulkBidSummaryProgressItemData(
        val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = BulkBidSummaryProgressItemDataKey
}

private const val BulkBidSummaryProgressItemDataKey = "progress"