package com.delhivery.orion.data.bids

import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.utils.extensions.safeEquals
import com.google.gson.annotations.SerializedName

data class TransactionBid(
  @SerializedName("supplier_id") val supplierId: String,
  @SerializedName("bid_status") val _status: String,
  @SerializedName("trip_completed") val tripCompleted: Boolean,
  @SerializedName("supplier_name") val supplierName: String,
  @SerializedName("creation_date") val creationDate: String,
  @SerializedName("updation_date") val updationDate: String,
  @SerializedName("latest_bid") val bidAmount: Int,
  @SerializedName("id") val id: String,
  @SerializedName("transaction_id") val transactionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = id

  /**
   * Formatted string for target price diff
   */
  fun targetPriceDiff(targetPrice: Int) = (targetPrice - bidAmount).let { _diff ->
    if (_diff == 0) return "(Same as target amount)"
    when (_diff > 0) {
      true -> "less"
      false -> "more"
    }.let { _x ->
      "(₹ ${Math.abs(_diff)} $_x than target price)"
    }
  }

  /**
   * Get status
   */
  fun status() = TransactionBidStatus.byStatusKey(_status)
}

enum class TransactionBidStatus(
  val statusKey: String,
  val status: String
) {
  Open("open", "Active"),
  Rejected("rejected", "Lost"),
  Accepted("accepted", "Confirmed"),
  NA("na", "NA");

  companion object {
    /**
     * Status by response keyg
     */
    fun byStatusKey(_status: String) =
      values().filter { it.statusKey.safeEquals(_status) }.firstOrNull() ?: Open
  }
}