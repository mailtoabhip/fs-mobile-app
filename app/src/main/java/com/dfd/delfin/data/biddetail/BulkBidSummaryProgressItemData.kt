package com.dfd.delfin.data.biddetail

import com.dfd.delfin.data.BaseKeyTypeModel


data class BulkBidSummaryProgressItemData(
        val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = BulkBidSummaryProgressItemDataKey
}

private const val BulkBidSummaryProgressItemDataKey = "progress"