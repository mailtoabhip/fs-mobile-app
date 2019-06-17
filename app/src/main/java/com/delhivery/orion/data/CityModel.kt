package com.delhivery.orion.data

import com.delhivery.orion.utils.StringUtils
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

  fun cityName() = StringUtils.capitalize(city)

  fun stateName() = StringUtils.capitalize(state ?: "")
}

/**
 * Convert city list to city names, string list
 */
fun List<CityModel>.names() =
  mapIndexed { _, cityModel ->
    val city: String = StringUtils.capitalize(cityModel.city)
    val state: String = StringUtils.capitalize(cityModel.state ?: "")
    when (state.length) {
      0 -> return@mapIndexed city
      else -> return@mapIndexed "$city, $state"
    }
  }

fun capitalize(str: String?) =
  (str?.substring(0, 1)?.toUpperCase() ?: "") + (str?.substring(1)?.toLowerCase() ?: "")
