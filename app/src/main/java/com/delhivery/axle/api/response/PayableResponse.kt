package com.delhivery.axle.api.response

import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.LedgerData
import com.google.gson.annotations.SerializedName

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
        @SerializedName("charge_amount") val chargeAmount: Double
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

data class ConsolidatedLedgerResponse(
        @SerializedName("count") val count: Int,
        @SerializedName("data") val ledgers: List<ConsolidatedLedgerItemData>
)

//data class TempConsolidatedLedgerResponse(
//        //@SerializedName("success") val success: Boolean,
//        @SerializedName("data") val data: ConsolidatedLedgerItemData
//        //@SerializedName("count") val count: Int
//)