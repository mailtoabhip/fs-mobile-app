package com.delhivery.axle.data.ledger

import com.delhivery.axle.data.BaseKeyTypeModel

data class ConsolidatedProgressItemData(
        val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = DashboardProgressItemDataKey
}

private const val DashboardProgressItemDataKey = "progress"