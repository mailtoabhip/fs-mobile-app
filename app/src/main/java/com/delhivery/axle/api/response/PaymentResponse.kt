package com.delhivery.axle.api.response

import com.delhivery.axle.api.response.ChargeType.Damages
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Data class to handle all responses from [com.delhivery.axle.api.PaymentService]
 *
 **
 */

data class TripChargesResponse(
  @SerializedName("username") val username: String,
  @SerializedName("head") val head: String,
  @SerializedName("bill_client") val billClient: Double,
  @SerializedName("pay_vendor") val payVendor: Double,
  @SerializedName("updation_date") val updationDate: String,
  @SerializedName("remarks") val remarks: String
) {

  fun chargeType() = ChargeType.byTypeId(head).charge

  fun charges() = when (head) {
    Damages.charge_key -> "- ₹ ${StringUtils.formatAmount(payVendor)}"
    else -> "₹ ${StringUtils.formatAmount(payVendor)}"
  }
}

data class TripPaymentsResponse(
  @SerializedName("items") val payments: List<TripPayment>,
  @SerializedName("total") val total: Int
)

data class TripPayment(
  @SerializedName("effective_price") val effectivePrice: Double,
  @SerializedName("advance_payout") val advancePayout: Double,
  @SerializedName("fuel_payout") val fuelPayout: Double?,
  @SerializedName("bid_price") val bidPrice: Double,
  @SerializedName("payments") val payments: Payments?,
  @SerializedName("transaction_id") val transactionId: String
)

data class Payments(
  @SerializedName("total_amount") val totalAmount: Double,
  @SerializedName("cash_advance") val cashAdvance: Double,
  @SerializedName("fuel_advance") val fuelAdvance: Double
)

enum class ChargeType(
  val charge_key: String,
  val charge: String
) {
  Freight("freight", "Freight"),
  Loading("loading_charge", "Loading Charge"),
  Unloading("unloading_charge", "Unloading Charge"),
  DetentionOrigin("detention_charge_origin", "Detention(Origin)"),
  DetentionDestination("detention_charge_destination", "Detention(Destination)"),
  Rto("rto", "RTO"),
  Damages("damage", "Damages"),
  ExtraRun("extra_run", "Extra Run"),
  TDS("tds", "TDS"),
  SubTotal("sub_total", "Sub Total"),
  Unknown("misc", "Miscellaneous");

  companion object {
    /**
     * Get [ChargeType] by type id
     */
    fun byTypeId(charge: String) =
      values().filter { it.charge_key == charge }.firstOrNull() ?: Unknown
  }
}