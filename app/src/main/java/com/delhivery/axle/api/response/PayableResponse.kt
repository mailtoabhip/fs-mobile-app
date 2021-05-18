package com.delhivery.axle.api.response

import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat

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
) {
  fun getChargeTitle(): String {
    return chargeHeadRef.replace("_", " ").capitalize()
  }
}

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

data class TripRecoveryResponse(
        @SerializedName("dn_id") val dnId: String,
        @SerializedName("trip_id") val tripId: String,
        @SerializedName("dn_type") val dnType: String,
        @SerializedName("total_recovery_amount") val TotalRecoveryAmount: Double,
        @SerializedName("pending_recovery") val pendingRecoveryAmount: Double,
        @SerializedName("recovery_data") val recoveryData: List<RecoveryData>?
)

data class RecoveryData(
        @SerializedName("recovery_amount") val recoveryAmount: Double,
        @SerializedName("trip_id") val recoveryTripId: String
)

data class OverpaymentRecoveryResponse(
  @SerializedName("utr") val utr: String,
  @SerializedName("trip_id") val tripId: String,
  @SerializedName("type") val Type: String,
  @SerializedName("total_recovery_amount") val TotalRecoveryAmount: Double,
  @SerializedName("pending_recovery") val pendingRecoveryAmount: Double,
  @SerializedName("recovery_data") val recoveryData: List<OverpaymentRecoveryData>?
)

data class OverpaymentRecoveryData(
  @SerializedName("recovery_amount") val recoveryAmount: Double,
  @SerializedName("trip_id") val recoveryTripId: String,
  @SerializedName("utr_number") val utrNumber: String
)



data class TDS(
        @SerializedName("amount") val amount: Double,
        @SerializedName("pmt_success_date") val pmtSuccessDate: String
){
    fun getTDS(
            tdsRate: Int,
            updatedTDSRate: Double
    ): Double{
        val date = "2020-05-16T23:59:00Z"
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val formattedDate = format.parse(date)

        if (amount > 0) {
            if (DateUtils.parseDate(pmtSuccessDate ?: "", DatePatterns.OrionDateFormat).after(formattedDate)) {
                return (amount * (updatedTDSRate / 100))
            } else {
                return (amount * (tdsRate.toDouble() / 100))
            }
        } else {
            return 0.0
        }
    }

    fun getTDSRate(
            tdsRate: Int,
            updatedTDSRate: Double
    ): Double{
        val date = "2020-05-16T23:59:00Z"
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val formattedDate = format.parse(date)

        if (amount > 0) {
            if (DateUtils.parseDate(pmtSuccessDate ?: "", DatePatterns.OrionDateFormat).after(formattedDate)) {
                return (updatedTDSRate / 100)
            } else {
                return (tdsRate.toDouble() / 100)
            }
        } else {
            return 0.0
        }
    }
}

data class ConsolidatedLedgerResponse(
        @SerializedName("count") val count: Int,
        @SerializedName("offset") val offset: Int?,
        @SerializedName("has_next") val hasNext: Boolean?,
        @SerializedName("consolidated") val ledgers: List<ConsolidatedLedgerItemData>
)

data class DownloadLedgerResponse(
        @SerializedName("url") val url: String,
        @SerializedName("bucket") val bucket: String
)

data class EmailLedgerResponse(
        @SerializedName("message") val message: String
)

data class InvoiceListResponse(
        @SerializedName("invoice_id") val invoiceId: String,
        @SerializedName("invoice_amount") val invoiceAmount: Double,
        @SerializedName("paid_amount") val paidAmount: Double
)