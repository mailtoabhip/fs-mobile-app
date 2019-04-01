package com.delhivery.orion.data

import com.google.gson.annotations.SerializedName

data class CityModel(
  @SerializedName("city") val city: String,
  @SerializedName("district") val district: String?,
  @SerializedName("city_type") val cityType: String?,
  @SerializedName("city_id") val cityId: String,
  @SerializedName("state") val state: String?,
  @SerializedName("state_id") val stateId: String?
) : BaseKeyTypeModel<String>() {
  override fun key() = cityId
}

/**
 * Convert city list to city names, string list
 */
fun List<CityModel>.names() = mapIndexed { _, cityModel -> cityModel.city }

