package com.delhivery.axle.api.response

import com.delhivery.axle.data.transactions.TransactionsItemData
import com.google.gson.annotations.SerializedName

/**
 * Wallet Response
 */
data class WalletDataResponse(@SerializedName("wallet") val wallet: WalletData)

data class WalletData(
  @SerializedName("active") val active: Boolean,
  @SerializedName("auto_withdraw") val autoWithdraw: Boolean,
  @SerializedName("balance") val balance: Double,
  @SerializedName("wallet_id") val walletId: String,
  @SerializedName("withdrawal_account_ifsc") val ifsc: String,
  @SerializedName("withdrawal_account_name") val accName: String,
  @SerializedName("withdrawal_account_number") val accNumber: String,
  @SerializedName("withdrawal_account_type") val accType: String
)

/**
 * Wallet Transactions Response
 */
data class WalletTransactionsResponse(
  @SerializedName("transactions") val transactions: List<TransactionsItemData>
)