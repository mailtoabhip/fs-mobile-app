package com.delhivery.axle.data.biddetail

import android.view.View
import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DrawableProviderUtils

data class BulkBidSummaryItemData(
    var vehicleType: String,
    var pmtRate: Double,
    var truckCount: Int,
    var status :String,
    var expanded: Boolean = false
): BaseKeyTypeModel<String>() {

    override fun key() = vehicleType

    fun visibility() = if(expanded)
        View.VISIBLE
    else
        View.GONE

    @DrawableRes
    fun toggleButton() = DrawableProviderUtils.expandedRes(expanded)

    fun pmtRate() = pmtRate.toString()

    fun truckCount() =truckCount.toString()

}
const val EXPAND_CARD = "expand"

