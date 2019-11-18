package com.delhivery.axle.data.bids

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.safeEquals
import com.google.gson.annotations.SerializedName
import kotlin.math.abs

data class TransactionBid(
  @SerializedName("supplier_id") val supplierId: String,
  @SerializedName("bid_status") val _status: String,
  @SerializedName("trip_completed") val tripCompleted: Boolean,
  @SerializedName("supplier_name") val supplierName: String,
  @SerializedName("creation_date") val creationDate: String,
  @SerializedName("updation_date") val updationDate: String,
  @SerializedName("latest_bid") val bidAmount: Double,
  @SerializedName("id") val id: String,
  @SerializedName("transaction_id") val transactionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = id

  /**
   * Formatted string for target price diff
   */
  fun targetPriceDiff(targetPrice: Int) = (targetPrice - bidAmount).let { _diff ->
    if (_diff.toInt() == 0) return "(Your Bid is same as target price)"
    when (_diff > 0) {
      true -> "less"
      false -> "more"
    }.let { _x ->
      "(Your Bid is ₹ ${StringUtils.formatAmount(abs(_diff))} $_x than target price)"
    }
  }

  /**
   * @return diff from lowest bid
   */
  fun diffFromLowestBid(
    lowestBid: Double,
    isPMTIndent: Boolean
  ): String {
    val bidText = if (isPMTIndent) {
      "PMT Bid"
    } else {
      "Bid"
    }
    return if (bidAmount > lowestBid) {
      "(Your $bidText is ₹ ${StringUtils.formatAmount(
          abs((bidAmount - lowestBid))
      )} more than lowest $bidText)"
    } else {
      "(Your $bidText is same as lowest $bidText)"
    }
  }

  /**
   * Get status
   */
  fun status() = TransactionBidStatus.byStatusKey(_status)
}

/**
 * Enum for Bid status
 */
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
     * Status by response key
     */
    fun byStatusKey(_status: String) =
      values().firstOrNull { it.statusKey.safeEquals(_status) } ?: Open
  }
}