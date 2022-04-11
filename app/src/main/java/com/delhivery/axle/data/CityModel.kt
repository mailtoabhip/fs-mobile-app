package com.delhivery.axle.data

import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CityModel(
  @SerializedName("city") val city: String,
  @SerializedName("orion_db_city_code") val orionDbCityCode: String? = "",
  @SerializedName("district") val district: String? = "",
  @SerializedName("state") val state: String? = ""
) : BaseKeyTypeModel<String>(),Serializable {
  override fun key() = orionDbCityCode ?: ""

  fun cityName() = StringUtils.capitalize(city) ?: ""

  fun stateName() = StringUtils.capitalize(state) ?: ""

  fun cityState(): String {
    val sb = StringBuilder().append(cityName())
    if (district.isNotNullOrEmpty())
      sb.append(", ").append(district)
    val code = orionDbCityCode?.subSequence(0, 2)
        .toString()
    StateModelList.toMutableList()
        .forEach { stateModel ->
          if (stateModel.stateId.compareTo(code) == 0) {
            sb.append(", ")
                .append(stateModel.stateId)
          }
        }
    return sb.toString()
  }

  fun getUserCity() = UserCity(city, orionDbCityCode)

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
  @SerializedName("orion_db_city_code") val dbCityCode: String? = ""
) : BaseKeyTypeModel<String>() {
  override fun key() = dbCityCode ?: ""

  fun cityName() = StringUtils.capitalize(city)

  fun stateName() = StringUtils.capitalize(state)

}

/**
 * Search city response
 */
data class SearchCitiesResponse(
  @SerializedName("records") val cities: List<SearchCityModel>
)
val CityStateModelList = listOf(
  CityModel("Andaman & Nicobar Islands", "AN", "","Andaman & Nicobar Islands"),
  CityModel("Andhra pradesh", "AP", "","Andhra pradesh"),
  CityModel("Arunachal pradesh", "AR", "","Arunachal pradesh"),
  CityModel("Assam", "AS", "","Assam"),
  CityModel("Bihar", "BR", "","Bihar"),
  CityModel("Chandigarh", "CH", "","Chandigarh"),
  CityModel("Chhattisgarh", "CG", "","Chhattisgarh"),
  CityModel("Dadra & nagar haveli", "DH DN", "","Dadra & nagar haveli"),
  CityModel("Daman & diu", "DD", "","Daman & diu"),
  CityModel("Delhi", "DL", "","Delhi"),
  CityModel("Goa", "GA", "","Goa"),
  CityModel("Gujarat", "GJ", "","Gujarat"),
  CityModel("Haryana", "HR", "","Haryana"),
  CityModel("Himachal pradesh", "HP", "","Himachal pradesh"),
  CityModel("Jammu & kashmir", "JK", "","Jammu & kashmir"),
  CityModel("Jharkhand", "JH", "","Jharkhand"),
  CityModel("Karnataka", "KA", "","Karnataka"),
  CityModel("Kerala", "KL", "","Kerala"),
  CityModel("Lakhswadeep", "LK LD", "","Lakhswadeep"),
  CityModel("Madhya pradesh", "MP", "","Madhya pradesh"),
  CityModel("Maharashtra", "MH", "","Maharashtra"),
  CityModel("Manipur", "MN", "","Manipur"),
  CityModel("Meghalaya", "ML", "","Meghalaya"),
  CityModel("Mizoram", "MZ", "","Mizoram"),
  CityModel("Nagaland", "NL", "","Nagaland"),
  CityModel("Odisha", "OR", "","Odisha"),
  CityModel("Puducherry", "PU", "","Puducherry"),
  CityModel("Punjab", "PB", "","Punjab"),
  CityModel("Rajasthan", "RJ", "","Rajasthan"),
  CityModel("Sikkim", "SK", "","Sikkim"),
  CityModel("Tamil nadu", "TN", "","Tamil nadu"),
  CityModel("Telangana", "TL", "","Telangana"),
  CityModel("Tripura", "TR", "","Tripura"),
  CityModel("Uttar pradesh", "UP", "","Uttar pradesh"),
  CityModel("Uttarakhand", "UK", "","Uttarakhand"),
  CityModel("West bengal", "WB", "","West bengal")
)
const val CitySelected = "city_selected"
