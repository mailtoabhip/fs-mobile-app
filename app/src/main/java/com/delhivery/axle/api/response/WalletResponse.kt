package com.delhivery.axle.api.response

import com.delhivery.axle.data.transactions.TransactionsItemData
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName

/**
 * Get Wallet Response
 */
data class WalletDataResponse(@SerializedName("wallet") val wallet: WalletData)

/**
 * Wallet Data
 */
data class WalletData(
  @SerializedName("active") val active: Boolean,
  @SerializedName("auto_withdraw") val autoWithdraw: Boolean,
  @SerializedName("balance") val balance: Double,
  @SerializedName("wallet_id") val walletId: String,
  @SerializedName("withdrawal_account_ifsc") val ifsc: String,
  @SerializedName("withdrawal_account_name") val accName: String,
  @SerializedName("withdrawal_account_number") val accNumber: String,
  @SerializedName("withdrawal_account_type") val accType: String,
  @SerializedName("optin_date_time") val optinDate: String?
) {

  /**
   * @return formatted balance
   */
  fun balance() = "₹ ${StringUtils.formatAmount(balance)}"

  /**
   * @return encrypted [accNumber]]
   */
  fun accNumber(): String {
    val encrypted = StringBuilder()
    val maskLength = accNumber.length - 4
    repeat((maskLength downTo 1).count()) { encrypted.append("*") }
    encrypted.append(accNumber.substring(maskLength))
    return encrypted.toString()
  }
}

/**
 * Wallet Transactions Response
 */
data class WalletTransactionsResponse(
  @SerializedName("transactions") val transactions: List<TransactionsItemData>
)

/**
 * Bank Transfer Response
 */
data class BankTransferResponse(
  @SerializedName("transaction_reference_number") val refNumber: String? = ""
)

/**
 * User Wallet Response (from GET /api/v1/wallet and POST /api/v1/wallet)
 */
data class UserWalletResponse(
    @SerializedName("wallet_id") val walletId: String = "",
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("current_balance") val currentBalance: String = "0.00",
    @SerializedName("min_th") val minThreshold: String = "0.00",
    @SerializedName("max_th") val maxThreshold: String = "0.00",
    @SerializedName("locked_amount") val lockedAmount: String = "0.00",
    @SerializedName("is_active") val isActive: Boolean = false,
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = ""
) : java.io.Serializable
