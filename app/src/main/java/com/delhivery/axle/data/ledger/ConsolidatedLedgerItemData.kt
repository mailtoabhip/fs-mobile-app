package com.delhivery.axle.data.ledger

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.annotation.DrawableRes
import com.delhivery.axle.api.response.ConsolidatedLedgerResponse
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ConsolidatedLedgerItemData(
        @SerializedName("payment_event") val paymentEvent: String,
        @SerializedName("amount") val amount: Double,
        @SerializedName("uuid") val uuid: String,
        @SerializedName("payment_type") val paymentType: String,
        @SerializedName("trip_id") val tripId: String,
        @SerializedName("lrs") val lrs: List<String>,
        @SerializedName("pmt_success_dt") val paymentSuccessDate: String,
        @SerializedName("utr_number") val utrNumber: String?,
        @SerializedName("month") val month: String,
        @SerializedName("deductions") val deductions: List<Map<String,Any>>,
        @SerializedName("invoice_id") val invoiceId: String,
        var expanded: Boolean = false

) : BaseKeyTypeModel<String>(), Serializable{

    override fun key() = tripId

    public fun getTitle():String{
        var capitalizeEvent = paymentEvent.substring(0,1).toUpperCase() + paymentEvent.substring(1)
        var isLRs = false
        if(lrs.size > 1){
            isLRs = true
        }
        var lrNo = " - "+lrs[0]
        if(isLRs){
            lrNo += " +"+lrs.size+" more"
        }
        return ""+capitalizeEvent+lrNo
    }

    public fun getAmount() = "+ Rs.$amount"

    public fun getUTR() = "UTR: UA12018"

    public fun getDeductionsTitle(index: Int): String{
        var ded_type = deductions.get(index)["deduction_type"]
        var title = ""
        if(ded_type == "tds_deduction"){
            title += "TDS (@1.5%)"
        }else if(ded_type == "dn_deduction"){
            title += "Deduction against LR "
            title += deductions.get(index)["lr_number"]
        }
        return title
    }

    public fun getDeductionsAmount(index: Int): String{
        var amount = deductions.get(index)["amount"]
        return "($amount)"
    }

    public fun getPaymentDate():String{

        return ""
    }

    public fun isExpanded() = expanded


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