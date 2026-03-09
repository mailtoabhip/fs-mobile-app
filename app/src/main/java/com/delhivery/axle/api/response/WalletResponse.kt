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
 * Wallet Transaction List Response (from /finance/users/wallet/transactions/list)
 * Wraps the paginated data inside BaseResponse.data
 */
data class WalletTransactionListResponse(
  @SerializedName("total_count") val totalCount: Int = 0,
  @SerializedName("page") val page: Int = 1,
  @SerializedName("per_page") val perPage: Int = 10,
  @SerializedName("transactions") val transactions: List<WalletTransactionItem> = emptyList()
)

/**
 * Individual wallet transaction item from the new listing API
 */
data class WalletTransactionItem(
  @SerializedName("transaction_id") val transactionId: String = "",
  @SerializedName("transaction_type") val transactionType: String = "",
  @SerializedName("amount") val amount: String = "0.0",
  @SerializedName("status") val status: String = "",
  @SerializedName("updated_wallet_balance") val updatedWalletBalance: String = "0.0",
  @SerializedName("ref_type") val refType: String = "",
  @SerializedName("created_at") val createdAt: String = "",
  @SerializedName("txn_details") val txnDetails: String? = null
) {
  /**
   * @return transaction heading based on ref_type
   */
  fun transactionHeading(): String {
    return when (refType.uppercase()) {
      "HYPERLOCAL" -> "Hyperlocal Payment"
      "WALLET_RECHARGE" -> "Wallet Recharge"
      "ORDER_PAYMENT" -> "Order Payment"
      "REFUND" -> "Refund"
      "FASTAG_RECHARGE" -> "FASTag Recharge"
      else -> refType.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }
    }
  }

  /**
   * @return amount as Double
   */
  fun amountDouble(): Double = amount.toDoubleOrNull() ?: 0.0
}


/**
 * User Wallet Response (from GET /finance/users/wallet/)
 */
data class UserWalletResponse(
    @SerializedName("vendor_id") val vendorId: String = "",
    @SerializedName("wallet_id") val walletId: String = "",
    @SerializedName("current_balance") val currentBalance: Double = 0.00,
    @SerializedName("is_active") val isActive: Boolean = false
) : java.io.Serializable {
    fun balanceFormatted(): String = "₹${StringUtils.formatAmount(currentBalance ?: 0.0)}"
}

/**
 * Single transaction status response (from GET /finance/users/wallet/transactions?txn_id=)
 */
data class WalletTransactionStatusResponse(
    @SerializedName("txn_id") val txnId: String = "",
    @SerializedName("transaction_type") val transactionType: String = "",
    @SerializedName("amount") val amount: String = "0.0",
    @SerializedName("status") val status: String = ""
)

/**
 * Wallet Recharge List Response (from GET /finance/users/wallet/recharge/transactions)
 */
data class WalletRechargeListResponse(
    @SerializedName("wallet_id") val walletId: String = "",
    @SerializedName("total") val total: Int = 0,
    @SerializedName("total_amount") val totalAmount: Double = 0.0,
    @SerializedName("opening_balance") val openingBalance: Double = 0.0,
    @SerializedName("recharges") val recharges: List<WalletRechargeItem> = emptyList()
)

/**
 * Individual recharge item
 */
data class WalletRechargeItem(
    @SerializedName("recharge_id") val rechargeId: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("status") val status: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("bank_refernce_no") val bankReferenceNo: String? = null,
    @SerializedName("added_via") val addedVia: String? = null
)

/**
 * Single recharge status response (from GET /finance/users/wallet/recharge/{recharge_id})
 */
data class WalletRechargeStatusResponse(
    @SerializedName("recharge_id") val rechargeId: String = "",
    @SerializedName("amount") val amount: String = "0.0",
    @SerializedName("status") val status: String = ""
)

