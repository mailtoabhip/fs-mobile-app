package com.delhivery.axle.data.ledger

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.annotation.DrawableRes
import com.delhivery.axle.api.response.ConsolidatedLedgerResponse
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils
import com.google.gson.annotations.SerializedName

class ConsolidatedLedgerItemData(
        val ledger: LedgerData,
        val expanded: Boolean = false

) : BaseKeyTypeModel<String>(){

    override fun key() = ledger.tripId

    /**
     * @return expanded resource basis [expanded]
     */
    @DrawableRes
    fun expandedResource() = DrawableProviderUtils.expandedResLedger(expanded)

}

enum class LedgerSpinnerOptions(val option: String, val key: Int, val value: String){
    DEFAULT("Recent Transactions", 0, "recent_transactions"),
    CURRENT_MONTH("Current Month", 1, "current_month"),
    PREVIOUS_MONTH("Previous Month", 2,"previous_month"),
    LAST_3_MONTH("Last 3 Months", 3,"last_3_months"),
    LAST_6_MONTH("Last 6 Months", 4,"last_6_months"),
    CURRENT_FIN_YEAR("Current Financial Year", 5,"current_financial_year")
}
const val ConsolidatedLedgerItemAction = "toggle_ledger"