package com.delhivery.axle.data.bids

import android.view.View
import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils

data class DmtBidSummaryItemData (
    var vehicleType: String,
    var pmtRate: Double,
    var truckCount: Int,
    var status :String,
    var expanded: Boolean = false,
    var deleted: Boolean = false

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

    const val DELETE_ITEM = "delete"
