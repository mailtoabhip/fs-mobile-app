package com.delhivery.axle.data

import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName

data class CityModel(
  @SerializedName("city") val city: String,
  @SerializedName("city_id") val cityId: String,
  @SerializedName("district") val district: String? = "",
  @SerializedName("city_type") val cityType: String? = "",
  @SerializedName("state") val state: String? = "",
  @SerializedName("state_id") val stateId: String? = ""
) : BaseKeyTypeModel<String>() {
  override fun key() = cityId

  fun cityName() = StringUtils.capitalize(city) ?: ""

  fun stateName() = StringUtils.capitalize(state) ?: ""

  fun cityState(): String {
    val sb = StringBuilder().append(cityName())
    val code = cityId.subSequence(0, 2)
        .toString()
    StateModelList.toMutableList()
        .forEach { stateModel ->
          if (stateModel.stateId.compareTo(code) == 0) {
            sb.append(", ")
                .append(stateModel.state)
          }
        }
    return sb.toString()
  }
}

data class CitiesResponse(
  @SerializedName("total") val totalBids: Int,
  @SerializedName("cities") val cities: List<CityModel>
)

/**
 * Convert city list to city names, string list
 */
fun List<CityModel>.names() =
  mapIndexed { _, cityModel ->
    val city: String = StringUtils.capitalize(cityModel.city) ?: ""
    val state: String = StringUtils.capitalize(cityModel.state) ?: ""
    when (state.length) {
      0 -> return@mapIndexed city
      else -> return@mapIndexed "$city, $state"
    }
  }