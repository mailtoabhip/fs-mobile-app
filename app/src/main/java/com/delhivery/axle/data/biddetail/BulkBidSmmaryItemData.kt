package com.delhivery.axle.data.biddetail

import android.view.View
import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty

data class BulkBidSummaryItemData(
    var vehicleType: String,
    var pmtRate: Double,
    var truckCount: Int,
    var openStatus:String?,
    var lowestBidStatus : Boolean =false,
    var expanded: Boolean = false,
    var confirmedStatus:String?=null,
    var lostStatus:String?=null,
    val vehicleNumber: MutableMap<String, String> = mutableMapOf<String, String>(),
    val childTransactionId: String?=null,
    val fromPage: String?=null

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

    fun openCount()=if(openStatus==null){""}else{openStatus}
    fun lostCount()=if(lostStatus==null){""}else{lostStatus}
    fun confirmedCount()=if(confirmedStatus==null){""}else{confirmedStatus}

    fun openStatusVisibility()= if(openStatus!=null && openStatus!!.isNotEmpty())
        View.VISIBLE
    else
        View.GONE
    fun confirmedStatusVisibility()= if(confirmedStatus!=null && confirmedStatus!!.isNotEmpty())
        View.VISIBLE
    else
        View.GONE
    fun lostStatusVisibility()= if(lostStatus!=null && lostStatus!!.isNotEmpty())
        View.VISIBLE
    else
        View.GONE
    fun lowestBidVisibility()=if(fromPage!=null && expanded && lowestBidStatus)
        View.VISIBLE
    else
        View.GONE


}
const val EXPAND_CARD = "expand"
const val OPEN_CONFIRMED_BID = "open"



