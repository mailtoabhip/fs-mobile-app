package com.delhivery.axle.data

import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName

data class CityModel(
  @SerializedName("city") val city: String,
  @SerializedName("city_code") val cityId: String,
  @SerializedName("orion_db_city_code") val orion_db_city_code: String? = "",
  @SerializedName("district") val district: String? = "",
  @SerializedName("state") val state: String? = "",
  @SerializedName("gn_state_code") val gnStateCode: String? = ""
) : BaseKeyTypeModel<String>() {
  override fun key() = cityId

  fun cityName() = StringUtils.capitalize(city) ?: ""

  fun stateName() = StringUtils.capitalize(state) ?: ""

  fun cityState(): String {
    val sb = StringBuilder().append(cityName())
    if (district.isNotNullOrEmpty())
      sb.append(", ").append(district)
    val code = orion_db_city_code?.subSequence(0, 2)
        .toString()
    StateModelList.toMutableList()
        .forEach { stateModel ->
          if (stateModel.gnStateCode.compareTo(code) == 0) {
            sb.append(", ")
                .append(stateModel.gnStateCode)
          }
        }
    return sb.toString()
  }

  fun getUserCity() = UserCity(city, cityId, orion_db_city_code)

}

data class CitiesResponse(
  @SerializedName("city_sugg") val cities: List<CityModel>
)

/**
 * Convert city list to city names, string list
 */
fun List<CityModel>.names() =
  mapIndexed { _, cityModel ->
    val city: String = StringUtils.capitalize(cityModel.city) ?: ""
    val district: String = StringUtils.capitalize(cityModel.district) ?: ""
    val state: String = StringUtils.capitalize(cityModel.state) ?: ""
    val sb = StringBuilder()
    sb.append(city)
    if (district.isNotNullOrEmpty())
      sb.append(", ").append(district)
    if (state.isNotNullOrEmpty())
      sb.append(", ").append(state)
    return@mapIndexed sb.toString()
  }

/**
 * Search city model
 */
data class SearchCityModel(
  @SerializedName("city") val city: String,
  @SerializedName("state") val state: String,
  @SerializedName("city_id") val cityId: String,
  @SerializedName("orion_db_city_code") val dbCityCode: String? = ""
) : BaseKeyTypeModel<String>() {
  override fun key() = cityId

  fun cityName() = StringUtils.capitalize(city)

  fun stateName() = StringUtils.capitalize(state)
}

/**
 * Search city response
 */
data class SearchCitiesResponse(
  @SerializedName("records") val cities: List<SearchCityModel>
)