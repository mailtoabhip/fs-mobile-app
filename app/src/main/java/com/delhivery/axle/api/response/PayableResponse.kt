package com.delhivery.axle.api.response

import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.LedgerData
import com.google.gson.annotations.SerializedName
import java.util.*

data class ChargesResponse(
        @SerializedName("payee_id") val payeeId: String,
        @SerializedName("remarks") val remarks: String,
        @SerializedName("payee_type") val payeeType: String,
        @SerializedName("trip_id") val tripId: String,
        @SerializedName("charge_status") val chargeStatus: String,
        @SerializedName("updated_at") val updatedAt: String,
        @SerializedName("action") val action: String,
        @SerializedName("updated_by") val updatedBy: String,
        @SerializedName("charge_head_ref") val chargeHeadRef: String,
        @SerializedName("balance_amount") val balanceAmount: Double,
        @SerializedName("charge_uuid") val chargeUuid: String,
        @SerializedName("charge_amount") val chargeAmount: Double,
        @SerializedName("days") val days: Int
)

data class DNResponse(
        @SerializedName("description") val description: String,
        @SerializedName("dn_id") val dnId: String,
        @SerializedName("created_by") val createdBy: String,
        @SerializedName("updated_by") val updatedBy: String,
        @SerializedName("booked_in_oracle") val bookedInOracle: Boolean,
        @SerializedName("invoice_no") val invoiceNo: String,
        @SerializedName("balance_amount") val balanceAmount: Double,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("amount") val amount: Double,
        @SerializedName("updated_at") val updatedAt: String,
        @SerializedName("payee") val payee: String,
        @SerializedName("status") val status: String
)

data class CollectionResponse(
        @SerializedName("uuid") val uuid: String,
        @SerializedName("dn_id") val dnId: String,
        @SerializedName("utr") val utr: String,
        @SerializedName("amount") val amount: Double,
        @SerializedName("payee_id") val payeeId: String,
        @SerializedName("type") val type: String
)

data class TDS(
        @SerializedName("amount") val amount: Double
){
    fun getTDS(
            tdsRate: Int,
            updatedTDSRate: Double
    ): Double{
        val tdsRelaxadtionDate = Calendar.getInstance()
        tdsRelaxadtionDate.set(Calendar.DAY_OF_MONTH, 16)
        tdsRelaxadtionDate.set(Calendar.MONTH, 4)
        tdsRelaxadtionDate.set(Calendar.YEAR, 2020)
        tdsRelaxadtionDate.set(Calendar.HOUR, 23)
        tdsRelaxadtionDate.set(Calendar.MINUTE, 59)
        if (amount > 0) {
            if (DateUtils.daysDiff(
                            DateUtils.parseDate("",DatePatterns.OrionDateFormat),
                            tdsRelaxadtionDate
                    ) > 0
            ) {
                return (amount * (updatedTDSRate / 100))
            } else {
                return (amount * (tdsRate / 100))
            }
        } else {
            return 0.0
        }
    }
}

data class ConsolidatedLedgerResponse(
        @SerializedName("count") val count: Int,
        @SerializedName("consolidated") val ledgers: List<ConsolidatedLedgerItemData>
)

data class DownloadLedgerResponse(
        @SerializedName("url") val url: String,
        @SerializedName("bucket") val bucket: String
)

data class EmailLedgerResponse(
        @SerializedName("message") val message: String
)