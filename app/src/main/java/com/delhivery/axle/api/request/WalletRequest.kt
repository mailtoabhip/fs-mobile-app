package com.delhivery.axle.api.request

import com.delhivery.axle.data.transactions.TransactionChannel
import com.google.gson.annotations.SerializedName

/**
 * Created by saurabhdhillon
 * for Delhivery Private Limited
 **
 *
 * Request creators for WalletService
 *
 **
 */
data class WalletUpdateRequest(
  @SerializedName("auto_withdraw") val autoWithdraw: Boolean
) {
  companion object {
    /**
     * @param autoWithdraw
     */
    fun getRequest(autoWithdraw: Boolean) = WalletUpdateRequest(autoWithdraw)
  }
}

data class BankTransferRequest(
  @SerializedName("amount") val ammount: String,
  @SerializedName("transaction_type") val type: String = "debit",
  @SerializedName("channel") val channel: String = TransactionChannel.ORACLE.type,
  @SerializedName("payment_method") val method: String = "imps"
) {
  companion object {
    /**
     * @param ammount
     */
    fun getRequest(ammount: String) = BankTransferRequest(ammount)
  }
}