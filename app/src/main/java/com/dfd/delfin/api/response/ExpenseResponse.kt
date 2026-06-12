package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

/**
 * Expense Data
 */
data class ExpenseData(
  @SerializedName("transaction_id") val transactionId: String,
  @SerializedName("charges") val charges: List<TripChargesResponse>,
  @SerializedName("payments") val payments: List<TripPaymentsResponse>,
  @SerializedName("total") val total: TotalExpense
)

/**
 * TotalExpense Data
 */
data class TotalExpense(
  @SerializedName("charges") val charges: Double,
  @SerializedName("advance_payment") val advancePayment: Double,
  @SerializedName("intermittent_payout") val intermittentPayout: Double?,
  @SerializedName("balance_payout") val balancePayout: Double,
  @SerializedName("total_payment") val totalPayment: Double
)