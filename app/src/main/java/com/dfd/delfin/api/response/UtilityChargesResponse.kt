package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class UtilityChargesResponse(
  @SerializedName("data") val chargeMap: Map<String, ChargesMapping>
)

data class ChargesMapping(
  @SerializedName("client_charges") val clientCharges: List<ReceivableCharges>,
  @SerializedName("vendor_charges") val vendorCharges: List<TripChargesResponse>
)

data class ReceivableCharges(
  @SerializedName("charge_head_ref") val head: String,
  @SerializedName("charge_amount") val chargeAmount: Double,
  @SerializedName("updated_at") val updationDate: String,
  @SerializedName("remarks") val remarks: String
)

data class ExpenseCharges(
  @SerializedName("charge_head_ref") val head: String,
  @SerializedName("pay_vendor") val payVendor: String?,
  @SerializedName("deduct_vendor") val deductVendor: String?,
  @SerializedName("updated_at") val updationDate: String,
  @SerializedName("remarks") val remarks: String
)



