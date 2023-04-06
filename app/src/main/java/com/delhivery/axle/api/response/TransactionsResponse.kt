package com.delhivery.axle.api.response

import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName

data class TransactionsResponse(
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("more_loads") val more_loads: Boolean,
  @SerializedName("load_price_percent") val loadPricePercent: Int,
  @SerializedName("total") val total: Int,
  @SerializedName("offset") val offset: Int,
  @SerializedName("active_count") val activeCount: Int?=0,
  @SerializedName("result") val transactions: List<HomeBidsRequestItemData>,
  @SerializedName("all_active_fetched") val allActiveFetched: Boolean?,
)

data class TruckDisplayNamesResponse(
  @SerializedName("truck_display_names") val truckDisplayNames: List<TruckDisplayNameItem>
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

  fun sum() = "₹ ${StringUtils.formatAmount(sum)}"
}

data class TruckDisplayNameItem(
  @SerializedName("key") val truckDisplayName:String
)
