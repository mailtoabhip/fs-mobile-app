package com.delhivery.axle.data.ledger

import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat

data class ConsolidatedLedgerItemData(
        @SerializedName("payment_event") val paymentEvent: String,
        @SerializedName("amount") val amount: Double,
        @SerializedName("uuid") val uuid: String,
        @SerializedName("payment_type") val paymentType: String,
        @SerializedName("trip_id") val tripId: String,
        @SerializedName("lrs") val lrs: List<String>?,
        @SerializedName("pmt_success_dt") val paymentSuccessDate: String,
        @SerializedName("utr_number") val utrNumber: String?,
        @SerializedName("month") val month: String,
        @SerializedName("deductions") val deductions: List<Map<String, Any>>,
        @SerializedName("invoice_id") val invoiceId: String?,
        var userType: String,
        var expanded: Boolean = false

) : BaseKeyTypeModel<String>(), Serializable{

    override fun key() = tripId

    public fun getTitle():String{
        var capitalizeEvent = paymentEvent.substring(0, 1).toUpperCase() + paymentEvent.substring(1)
        if(capitalizeEvent == "Loading"){
            capitalizeEvent = "Advance"
        }
        var isLRs = false
        if (lrs != null) {
            if(lrs.size > 1){
                isLRs = true
            }
        }
        var lrNo = " - "+ (lrs?.get(0) ?: "")
        if (lrs != null) {
            if(isLRs){
                lrNo += " +"+(lrs.size-1)+" more"
            }
        }
        return ""+capitalizeEvent+lrNo
    }

    public fun getAmount() = "+ Rs.$amount"

    public fun getUTR() = "UTR: UA12018"

    public fun getDeductionsTitle(index: Int): String{
        var ded_type = deductions.get(index)["deduction_type"]
        var lr_number = deductions.get(index)["lr_number"].toString()
        val listLRs: List<String> = lr_number.split(",").toList()

        var title = ""
        if(ded_type == "tds_deduction"){
            if(userType == "individual"){
                title += "TDS (@0.75%)"
            }else{
                title += "TDS (@1.5%)"
            }
        }else if(ded_type == "dn_deduction"){
            title += "Recovery against LR "
            title += listLRs[0]
            if(listLRs.size > 1){
                title += ",\n"
                var i = 0
                for (lr in listLRs){
                    if(i == listLRs.size - 1){
                        title += lr
                    }else if(i != 0){
                        title += "$lr, "
                    }
                    i += 1
                }
            }
        }
        return title
    }

    public fun getDeductionsAmount(index: Int): String{
        var amount = deductions.get(index)["amount"]
        amount = String.format("%.2f",amount)
        return "($amount)"
    }

    public fun getPaymentDate():String{
        var year = paymentSuccessDate.substring(0, 4)
        var month = paymentSuccessDate.substring(5, 7).toInt()
        var date = paymentSuccessDate.substring(8, 10)

        var monthString = DateFormatSymbols().months[month - 1]


        return "$date $monthString $year"
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
    PREVIOUS_MONTH("Previous Month", 2, "previous_month"),
    LAST_3_MONTH("Last 3 Months", 3, "last_3_months"),
    LAST_6_MONTH("Last 6 Months", 4, "last_6_months"),
    CURRENT_FIN_YEAR("Current Financial Year", 5, "current_financial_year")
}
const val ConsolidatedLedgerItemAction = "toggle_ledger"