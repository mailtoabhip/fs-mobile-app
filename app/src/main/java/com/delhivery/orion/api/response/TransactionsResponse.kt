package com.delhivery.orion.api.response

import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.google.gson.annotations.SerializedName

data class TransactionsResponse(
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("total") val total: Int,
  @SerializedName("offset") val offset: Int,
  @SerializedName("result") val transactions: List<HomeBidsRequestItemData>
)

data class TripMeterResponse(
  @SerializedName("1") val jan: MonthlyEarning? = null,
  @SerializedName("2") val feb: MonthlyEarning? = null,
  @SerializedName("3") val mar: MonthlyEarning? = null,
  @SerializedName("4") val apr: MonthlyEarning? = null,
  @SerializedName("5") val may: MonthlyEarning? = null,
  @SerializedName("6") val jun: MonthlyEarning? = null,
  @SerializedName("7") val jul: MonthlyEarning? = null,
  @SerializedName("8") val aug: MonthlyEarning? = null,
  @SerializedName("9") val sep: MonthlyEarning? = null,
  @SerializedName("10") val oct: MonthlyEarning? = null,
  @SerializedName("11") val nov: MonthlyEarning? = null,
  @SerializedName("12") val dec: MonthlyEarning? = null
)

data class MonthlyEarning(
  @SerializedName("count") val count: Int,
  @SerializedName("sum") val sum: Double
) {

  fun count() = "$count trips"

  fun sum() = "₹ ${String.format("%, .0f", sum)}"
}