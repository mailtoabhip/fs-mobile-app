package com.dfd.delfin.data.ledger

import com.dfd.delfin.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName


data class LedgerData(
        @SerializedName("payment_event") val paymentEvent: String,
        @SerializedName("amount") val amount: Double,
        @SerializedName("uuid") val uuid: String,
        @SerializedName("payment_type") val paymentType: String,
        @SerializedName("trip_id") val tripId: String,
        @SerializedName("lrs") val lrs: List<String>,
        @SerializedName("pmt_success_dt") val paymentSuccessDate: String,
        @SerializedName("utr_number") val utrNumber: String,
        @SerializedName("month") val month: String,
        @SerializedName("deductions") val deductions: List<Map<String,Any>>,
        @SerializedName("invoice_id") val invoiceId: String
): BaseKeyTypeModel<String>() {
    override fun key() = tripId
}



