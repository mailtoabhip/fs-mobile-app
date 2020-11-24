package com.delhivery.axle.api.response

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
        @SerializedName("balance_amount") val balanceAmount: Int,
        @SerializedName("charge_uuid") val chargeUuid: String,
        @SerializedName("charge_amount") val chargeAmount: Int
)

data class DNResponse(
        @SerializedName("booked_in_oracle") val bookedInOracle: Boolean,
        @SerializedName("status") val status: String,
        @SerializedName("invoice_no") val invoiceNo: String,
        @SerializedName("dn_id") val dnId: String,
        @SerializedName("leftover_amount") val leftoverAmount: Int,
        @SerializedName("trip_id") val tripId: String,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("amount") val amount: Int,
        @SerializedName("balance_amount") val balanceAmount: Int,
        @SerializedName("payee") val payee: String,
        @SerializedName("updated_by") val updatedBy: String,
        @SerializedName("description") val description: String,
        @SerializedName("updated_at") val updatedAt: String,
        @SerializedName("created_by") val createdBy: String
)