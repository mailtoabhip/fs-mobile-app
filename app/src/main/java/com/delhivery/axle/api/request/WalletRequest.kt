package com.delhivery.axle.api.request

import com.delhivery.axle.data.transactions.TransactionChannel
import com.delhivery.axle.data.transactions.TransactionType
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

/**
 * Wallet activate request creator
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

/**
 * Bank Transfer request creator
 */
data class BankTransferRequest(
  @SerializedName("amount") val amount: String,
  @SerializedName("transaction_type") val type: String = "debit",
  @SerializedName("channel") val channel: String = TransactionChannel.ORACLE.type,
  @SerializedName("payment_method") val method: String = "imps"
) {
  companion object {
    /**
     * @param amount
     */
    fun getRequest(amount: String) = BankTransferRequest(amount)
  }
}

/**
 * Fuel card request creator
 */
data class CreateFuelCardRequest(
  @SerializedName("to_account_number") val toAccNumber: String,
  @SerializedName("to_bank_ifsc") val toIfsc: String,
  @SerializedName("trip_id") val tripId: String,
  @SerializedName("vehicle_number") val vehicleNum: String,
  @SerializedName("amount") val amount: String,
  @SerializedName("transaction_type") val transactionType: String = TransactionType.DEBIT.type,
  @SerializedName("channel") val channel: String = TransactionChannel.IOCL.type
) {
  companion object {
    /**
     * @param [toAccNumber] [toIfsc] [tripId] [vehicleNum] [amount]
     */
    fun getRequest(
      toAccNumber: String,
      toIfsc: String,
      tripId: String,
      vehicleNum: String,
      amount: String
    ) = CreateFuelCardRequest(toAccNumber, toIfsc, tripId, vehicleNum, amount)
  }
}