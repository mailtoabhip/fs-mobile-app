package com.delhivery.axle.data.ledger

import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils

class ConsolidatedMonthItemData(
        val month: String,
        val monthId: Int,
        var ledgerData: List<ConsolidatedLedgerItemData>,
        var expanded: Boolean = false
) : BaseKeyTypeModel<String>() {
    override fun key() = month

    fun month() = StringUtils.capitalize(month)

    /**
     * @return expanded resource basis [expanded]
     */
    @DrawableRes
    fun expandedResource() = DrawableProviderUtils.expandedRes(expanded)
}

const val ConsolidatedMonthItemAction = "toggle"