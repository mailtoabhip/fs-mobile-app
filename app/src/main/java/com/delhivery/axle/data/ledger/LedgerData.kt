package com.delhivery.axle.data.ledger

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class LedgerData(
    @SerializedName("payment_event") val paymentEvent: String,
    @SerializedName("invoice_id") val invoiceId: String,
    @SerializedName("payment_type") val paymentType: String,
    @SerializedName("deductions") val deductions: Map<String,Double>,
    @SerializedName("utr") val utr: String,
    @SerializedName("payment_success_date") val paymentSuccessDate: String,
    @SerializedName("lrs") val lrs: List<String>,
    @SerializedName("trip_id") val tripId: String,
    @SerializedName("amount") val amount: Double
): BaseKeyTypeModel<String>() {
    override fun key() = tripId
}



