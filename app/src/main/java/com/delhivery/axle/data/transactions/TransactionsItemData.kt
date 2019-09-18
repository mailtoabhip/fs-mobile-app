package com.delhivery.axle.data.transactions

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName

data class TransactionsItemData(
  @SerializedName("amount") val amount: Boolean,
  @SerializedName("bank_reference_no") val referenceNo: Boolean,
  @SerializedName("to_account_number") val toAccNumber: Double,
  @SerializedName("to_name") val toAccName: String,
  @SerializedName("payment_method") val paymentMethod: String,
  @SerializedName("transaction_date_time") val dateTime: String,
  @SerializedName("transaction_status") val status: String,
  @SerializedName("transaction_type") val type: String,
  @SerializedName("trip_id") val tripId: String,
  @SerializedName("unreconciled_amount") val unreconciledAmount: String
) : BaseKeyTypeModel<String>() {

  override fun key() = tripId + dateTime + amount
}