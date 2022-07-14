package com.delhivery.axle.data.sharerates

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName

data class ShareRateRoutesItemData(
  @SerializedName("origin_city_code") val originCityCode: String?=null,
  @SerializedName("origin_city") val originCity: String?=null,
  @SerializedName("destination_city_code") val destinationCityCode: String?=null,
  @SerializedName("destination_city") val destinationCity: String?=null,
  @SerializedName("truck_display_name") val truckDisplayName: String?=null,
  @SerializedName("truck_capacity") val truckCapacity: String?=null,
  @SerializedName("offer_type") val offerType: String?=null,
  @SerializedName("status") val status: String?=null,
  @SerializedName("id") val offerId: String?=null

): BaseKeyTypeModel<String>() {
  override fun key() = originCityCode + ":" + destinationCityCode + ":" + truckDisplayName
  /**
   * @return formatted origin city name
   */
  fun originCityName() = StringUtils.capitalize(originCity) ?: ""

  /**
   * @return formatted destination city name
   */
  fun destinationCityName() = StringUtils.capitalize(destinationCity) ?: ""

  fun routes(): String {
    val stopBuilder = StringBuilder()
    stopBuilder.append(originCityName())
      .append(" - ")
    stopBuilder.append(destinationCityName())
    return stopBuilder.toString()
  }

}
const val ShareRatesItemDataAction_ViewDetails = "share_rate"