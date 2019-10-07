package com.delhivery.axle.data.transactions

import android.text.TextUtils
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.transactions.TransactionChannel.HPCL
import com.delhivery.axle.data.transactions.TransactionChannel.IOCL
import com.delhivery.axle.data.transactions.TransactionChannel.ORACLE
import com.delhivery.axle.data.transactions.TransactionChannel.UNKNOWN
import com.delhivery.axle.data.transactions.TransactionType.ADVANCE_AUTO_DEBIT
import com.delhivery.axle.data.transactions.TransactionType.ADVANCE_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.DEBIT
import com.delhivery.axle.data.transactions.TransactionType.DEBIT_NOTE
import com.delhivery.axle.data.transactions.TransactionType.PETRO_CASHBACK_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.PETRO_CASHBACK_DEBIT
import com.delhivery.axle.data.transactions.TransactionType.PETRO_REFUND_CREDIT
import com.delhivery.axle.data.transactions.TransactionType.RECONCILIATION_DEBIT
import com.delhivery.axle.utils.ColorProviderUtils
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.bind.util.ISO8601Utils
import java.io.Serializable
import java.text.ParsePosition

data class TransactionsItemData(
  @SerializedName("amount") val amount: Double,
  @SerializedName("bank_reference_no") val referenceNo: String? = "",
  @SerializedName("to_account_number") val toAccNumber: String? = "",
  @SerializedName("to_name") val toAccName: String? = "",
  @SerializedName("from_account_number") val fromAccNumber: String? = "",
  @SerializedName("from_name") val fromAccName: String? = "",
  @SerializedName("bank_remarks") val bankRemarks: String? = "",
  @SerializedName("delhivery_remarks") val delhiveryRemarks: String? = "",
  @SerializedName("payment_method") val paymentMethod: String,
  @SerializedName("transaction_date_time") val dateTime: String,
  @SerializedName("transaction_status") val status: String,
  @SerializedName("transaction_type") val type: String,
  @SerializedName("transaction_reference_number") val transactionNumber: String,
  @SerializedName("channel") val channel: String? = "",
  @SerializedName("trip_id") val tripId: String? = "",
  @SerializedName("unreconciled_amount") val unreconciledAmount: String,
  @SerializedName("vehicle_number") val vehicleNumber: String? = ""
) : BaseKeyTypeModel<String>(), Serializable {

  override fun key() = type + (tripId ?: "") + dateTime + amount

  fun transactionHeading() =
    when (transactionType()) {
      DEBIT -> {
        when (TransactionChannel.byType(channel ?: "")) {
          ORACLE -> "Transferred to Bank"
          IOCL, HPCL -> "Transferred for Fuel"
          UNKNOWN -> transactionType().type
        }
      }
      ADVANCE_CREDIT -> "Advance Received"
      PETRO_REFUND_CREDIT -> "Fuel Credit Revert"
      RECONCILIATION_DEBIT -> "Advance amount auto withdrawal"
      PETRO_CASHBACK_CREDIT -> "Fuel Cashback Credit"
      PETRO_CASHBACK_DEBIT -> "Fuel Cashback auto withdrawal"
      ADVANCE_AUTO_DEBIT -> "Advance Transferred Automatically"
      DEBIT_NOTE -> "Debit Note"
      else -> transactionType().type
    }

  fun subLabel() =
    if (TextUtils.isEmpty(failedStatus())) {
      when (transactionType()) {
        DEBIT -> {
          when (TransactionChannel.byType(channel ?: "")) {
            ORACLE -> accNumber()
            IOCL, HPCL -> "$vehicleNumber, $toAccNumber"
            UNKNOWN -> transactionType().type
          }
        }
        ADVANCE_CREDIT -> vehicleNumber
        PETRO_REFUND_CREDIT -> "$vehicleNumber, $toAccNumber"
        RECONCILIATION_DEBIT -> "$vehicleNumber($toAccNumber)"
        PETRO_CASHBACK_CREDIT -> "$vehicleNumber, $toAccNumber"
        PETRO_CASHBACK_DEBIT -> "${accNumber()}, $vehicleNumber"
        ADVANCE_AUTO_DEBIT -> "${accNumber()}, $vehicleNumber"
        DEBIT_NOTE -> "$vehicleNumber, $toAccNumber"
        else -> transactionType().type
      }
    } else {
      failedStatus()
    }

  fun transactionType() = TransactionType.byType(type)

  fun balanceHeading() = when {
    isCredit() -> "Amount credited"
    else -> "Amount debited"
  }

  private fun isCredit() = type.toLowerCase().contains("credit")

  fun status() = when (status) {
    "processing", "pending" -> "PROCESSING"
    "failed", "rejected" -> "FAILED"
    else -> "PROCESSED"
  }

  fun failedStatus() = when (status) {
    "failed", "rejected" -> "FAILED"
    else -> ""
  }

  fun amountAndSymbol() = if (isCredit()) {
    "+ "
  } else {
    "- "
  } + "₹ " + StringUtils.formatAmount(amount)

  fun amount() = "₹ " + StringUtils.formatAmount(amount)

  fun cashback() = "Cashback: ₹ " + StringUtils.formatAmount(amount * 3 / 100)

  /**
   * @return encrypted [toAccNumber]]
   */
  fun accNumber() =
    if (toAccNumber.isNotNullOrEmpty()) {
      val encrypted = StringBuilder()
      val maskLength = (toAccNumber?.length ?: 4) - 4
      repeat((maskLength downTo 1).count()) { encrypted.append("*") }
      encrypted.append(toAccNumber?.substring(maskLength))
      encrypted.toString()
    } else {
      "Not Available"
    }

  @ColorRes
  fun requiredAmountColor() = ColorProviderUtils.getTransactionAmountColor(type)

  @ColorRes
  fun requiredStatusColor() = ColorProviderUtils.getTransactionStatusColor(status)

  @DrawableRes
  fun transactionTypeDrawableRes() = DrawableProviderUtils.transactionTypeDrawableRes(
      transactionType(), TransactionChannel.byType(channel ?: "")
  )

  fun dateTime() = DateUtils.formatISODate(dateTime, "HH:mm dd-MMM-YYY")

}

enum class TransactionType(val type: String) {
  DEBIT("debit"),
  DEBIT_NOTE("debitnote-debit"),
  ADVANCE_AUTO_DEBIT("advance-auto-debit"),
  PETRO_CASHBACK_DEBIT("petro-cashback-debit"),
  RECONCILIATION_DEBIT("reconciliation-debit"),
  BALANCE_DEBIT("balance-debit"),
  CREDIT("credit"),
  REWARD_CREDIT("reward-credit"),
  BALANCE_CREDIT("balance-credit"),
  ADVANCE_CREDIT("advance-credit"),
  PETRO_REFUND_CREDIT("petro-refund-credit"),
  PETRO_CASHBACK_CREDIT("petro-cashback-credit"),
  UNKNOWN("unknown");

  companion object {
    /**
     * Get [TransactionType] by type
     */
    fun byType(type: String) = values().firstOrNull { type == it.type } ?: UNKNOWN
  }
}

enum class TransactionChannel(val type: String) {
  IOCL("iocl"),
  HPCL("hpcl"),
  ORACLE("oracle"),
  UNKNOWN("unknown");

  companion object {
    /**
     * Get [TransactionChannel] by type
     */
    fun byType(type: String) = values().firstOrNull { type == it.type } ?: UNKNOWN
  }
}

class TransactionComparator : Comparator<TransactionsItemData> {

  override fun compare(
    o1: TransactionsItemData?,
    o2: TransactionsItemData?
  ): Int {
    if (o2 == null) return 1
    if (o1 == null) return -1
    return if (ISO8601Utils.parse(o1.dateTime, ParsePosition(0)).before(
            ISO8601Utils.parse(o2.dateTime, ParsePosition(0))
        )
    ) 1 else -1
  }
}

/* actions */
const val TransactionAction_ViewDetails = "transaction_details"