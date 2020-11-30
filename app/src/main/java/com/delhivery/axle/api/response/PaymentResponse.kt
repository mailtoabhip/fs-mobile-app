package com.delhivery.axle.api.response

import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName
import java.util.Calendar
import kotlin.math.abs

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
  @SerializedName("charge_head_ref") val head: String,
  @SerializedName("pay_vendor") val payVendor: Double?,
  @SerializedName("deduct_vendor") val deductVendor: Double?,
  @SerializedName("updation_date") val updationDate: String? = null,
  @SerializedName("remarks") val remarks: String
) {
  /**
   * Return charge type
   */
  fun chargeType() = StringUtils.capitalize(head.replace("_", " "))

  /**
   * return charge
   */
  fun charge() = when {
    payVendor != null && payVendor != 0.0 -> {
      if (payVendor > 0) "₹ ${StringUtils.formatAmount(abs(payVendor))}"
      else "- ₹ ${StringUtils.formatAmount(abs(payVendor))}"
    }
    deductVendor != null && deductVendor != 0.0 -> {
      "- ₹ ${StringUtils.formatAmount(abs(deductVendor))}"
    }
    else -> {
      ""
    }
  }
}

/**
 * Trip Payments Bulk Response
 */
data class TripPaymentsBulkResponse(
  @SerializedName("items") val payments: List<BulkPaymentItem>,
  @SerializedName("total") val total: Int
)

/**
 * Bulk Payment Item
 */
data class BulkPaymentItem(
  @SerializedName("effective_price") val effectivePrice: Double,
  @SerializedName("advance_payout") val advancePayout: Double,
  @SerializedName("fuel_payout") val fuelPayout: Double?,
  @SerializedName("bid_price") val bidPrice: Double,
  @SerializedName("transaction_id") val transactionId: String
)

/**
 * New Payments Response
 */

data class  PaymentsResponse(
        @SerializedName("status") val status: String,
        @SerializedName("username") val username: String,
        @SerializedName("head") val head: String,
        @SerializedName("oracle_unique_id") val oracleUniqueId: String,
        @SerializedName("uuid") val uuid: String?,
        @SerializedName("transfer_time") val transferTime: String?,
        @SerializedName("utr_number") val utrNumber: String?,
        @SerializedName("remarks") val remarks: String? = "",
        @SerializedName("amount") val amount: Double,
        @SerializedName("payment_type") val paymentType: String,
        @SerializedName("updation_date") val updationDate: String,
        @SerializedName("payment_mode") val paymentMode: String?,
        @SerializedName("lr_nos") val lr_nos: List<String>,
        @SerializedName("transaction_id") val transactionId: String = ""
)

/**
 * Trip Payments Response
 */
data class TripPaymentsResponse(
  @SerializedName("head") var head: String,
  @SerializedName("bank_transaction_id") val bankTransactionId: String,
  @SerializedName("amount") var amount: Double,
  @SerializedName("transfer_time") val transferTime: String?,
  @SerializedName("remarks") var remark: String? = ""
) {

  /**
   * Return charge type
   */
  fun chargeType() = StringUtils.capitalize(head.replace("_", " "))

  /**
   * Return charge
   */
  fun charge() = "₹ ${StringUtils.formatAmount(amount)}"

  /**
   * Relative Time stamp
   */
  fun timeStamp() = transferTime?.let { DateUtils.convertToRelativeTimeStamp(it) } ?: ""

  /**
   * Time Stamp
   */
  fun dateTime() =
    transferTime?.let {
      "Paid on: " + DateUtils.formatDate(
          DateUtils.parseDate(it, DatePatterns.OrionDateFormat),
          DatePatterns.SimpleDateFormat
      )
    } ?: ""

  /**
   * Bank transaction number
   */
  fun utr() = "UTR no: $bankTransactionId"

  /**
   * Get tds
   */
  fun getTDS(
    tdsRate: Int,
    updatedTdsRate: Double
  ): Double {
    val tdsRelaxadtionDate = Calendar.getInstance()
    tdsRelaxadtionDate.set(Calendar.DAY_OF_MONTH, 16)
    tdsRelaxadtionDate.set(Calendar.MONTH, 4)
    tdsRelaxadtionDate.set(Calendar.YEAR, 2020)
    tdsRelaxadtionDate.set(Calendar.HOUR, 23)
    tdsRelaxadtionDate.set(Calendar.MINUTE, 59)
    if (amount > 0) {
      if (DateUtils.daysDiff(
              DateUtils.parseDate(transferTime ?: "", DatePatterns.OrionDateFormat),
              tdsRelaxadtionDate
          ) > 0
      ) {
        return (amount * (100 - updatedTdsRate) / 100)
      } else {
        return (amount * (100 - tdsRate) / 100)
      }
    } else {
      return 0.0
    }
  }

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
        values().firstOrNull { it.charge_key == charge } ?: Unknown
    }
  }
}