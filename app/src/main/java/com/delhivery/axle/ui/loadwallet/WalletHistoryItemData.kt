package com.delhivery.axle.ui.loadwallet

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data model for wallet history items
 */
data class WalletHistoryItemData(
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("transaction_date_time") val dateTime: String,
    @SerializedName("transaction_status") val status: String,
    @SerializedName("transaction_reference_number") val txnNumber: String,
    @SerializedName("transaction_type") val type: String,
    @SerializedName("bank_reference_no") val bankReferenceNo: String? = null,
    @SerializedName("added_via") val addedVia: String? = null,
    @SerializedName("txn_details") val txnDetails: String? = null
) : BaseKeyTypeModel<String>(), Serializable {

    override fun key() = txnNumber + dateTime

    /**
     * @return formatted amount with sign
     */
    fun amountFormatted(): String {
        val sign = if (isCredit()) "+" else "-"
        return "${sign}₹${StringUtils.formatAmount(amount)}"
    }

    /**
     * @return formatted date string
     */
    fun dateFormatted(): String = DateUtils.formatISODate(dateTime, "dd MMM yyyy | hh:mm a")

    /**
     * @return transaction number label
     */
    fun txnLabel(): String = "TXN: $txnNumber"

    /**
     * @return status display label for list cards
     */
    fun statusLabel(): String = when (status.lowercase()) {
        "success", "processed" -> "SUCCESS"
        "pending", "processing" -> "PENDING"
        "failed", "failure", "rejected" -> "FAILED"
        else -> status.uppercase()
    }

    /**
     * @return status display label for detail screen
     */
    fun detailStatusLabel(): String = when (status.lowercase()) {
        "success", "processed" -> "SUCCESS"
        "pending", "processing" -> "Pending"
        "failed", "failure", "rejected" -> "Failed Transaction"
        else -> status.uppercase()
    }

    /**
     * @return true if transaction is pending
     */
    fun isPending(): Boolean = status.lowercase() in listOf("pending", "processing")

    /**
     * @return true if transaction is failed
     */
    fun isFailed(): Boolean = status.lowercase() in listOf("failed", "failure", "rejected")

    /**
     * @return color resource for status text
     */
    @ColorRes
    fun statusColorRes(): Int = when (status.lowercase()) {
        "success" -> R.color.txn_success
        "pending" -> R.color.pending_status
        "failure", "failed" -> R.color.status_lost_bid
        else -> R.color.heading_black
    }

    /**
     * @return drawable resource for status badge background
     */
    @DrawableRes
    fun statusBgRes(): Int = when (status.lowercase()) {
        "success", "processed" -> R.drawable.bg_status_pill_success
        "pending", "processing" -> R.drawable.bg_status_pill_pending
        "failed", "failure", "rejected" -> R.drawable.bg_status_pill_failed
        else -> R.drawable.bg_status_pill_pending
    }

    /**
     * @return color resource for amount text
     */
    @ColorRes
    fun amountColorRes(): Int = when {
        status.lowercase() in listOf("failed", "failure", "rejected") -> R.color.font_labels
        isCredit() -> R.color.reward_status_confirmed
        else -> R.color.heading_black
    }

    @ColorRes
    fun txnAmountColorRes(): Int = when {
        status.lowercase() in listOf("failed", "failure") -> R.color.font_labels
        else -> R.color.heading_black
    }

    private fun isCredit() = type.lowercase().contains("credit")
}
