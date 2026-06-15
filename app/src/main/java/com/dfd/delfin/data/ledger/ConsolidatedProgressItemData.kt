package com.dfd.delfin.data.ledger

import com.dfd.delfin.data.BaseKeyTypeModel

data class ConsolidatedProgressItemData(
        val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = DashboardProgressItemDataKey
}

private const val DashboardProgressItemDataKey = "progress"