package com.delhivery.axle.data

import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CityModel(
  @SerializedName("city") val city: String,
  @SerializedName("orion_db_city_code") val orionDbCityCode: String? = "",
  @SerializedName("district") val district: String? = "",
  @SerializedName("state") val state: String? = "",
  val type:String = "city"
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
  CityModel("Andaman & Nicobar Islands", "AN", "","Andaman & Nicobar Islands","state"),
  CityModel("Andhra pradesh", "AP", "","Andhra pradesh","state"),
  CityModel("Arunachal pradesh", "AR", "","Arunachal pradesh","state"),
  CityModel("Assam", "AS", "","Assam","state"),
  CityModel("Bihar", "BR", "","Bihar","state"),
  CityModel("Chandigarh", "CH", "","Chandigarh","state"),
  CityModel("Chhattisgarh", "CG", "","Chhattisgarh","state"),
  CityModel("Dadra & nagar haveli", "DH DN", "","Dadra & nagar haveli","state"),
  CityModel("Daman & diu", "DD", "","Daman & diu","state"),
  CityModel("Delhi", "DL", "","Delhi","state"),
  CityModel("Goa", "GA", "","Goa","state"),
  CityModel("Gujarat", "GJ", "","Gujarat","state"),
  CityModel("Haryana", "HR", "","Haryana","state"),
  CityModel("Himachal pradesh", "HP", "","Himachal pradesh","state"),
  CityModel("Jammu & kashmir", "JK", "","Jammu & kashmir","state"),
  CityModel("Jharkhand", "JH", "","Jharkhand","state"),
  CityModel("Karnataka", "KA", "","Karnataka","state"),
  CityModel("Kerala", "KL", "","Kerala","state"),
  CityModel("Lakhswadeep", "LK LD", "","Lakhswadeep","state"),
  CityModel("Madhya pradesh", "MP", "","Madhya pradesh","state"),
  CityModel("Maharashtra", "MH", "","Maharashtra","state"),
  CityModel("Manipur", "MN", "","Manipur","state"),
  CityModel("Meghalaya", "ML", "","Meghalaya","state"),
  CityModel("Mizoram", "MZ", "","Mizoram","state"),
  CityModel("Nagaland", "NL", "","Nagaland","state"),
  CityModel("Odisha", "OR", "","Odisha","state"),
  CityModel("Puducherry", "PU", "","Puducherry","state"),
  CityModel("Punjab", "PB", "","Punjab","state"),
  CityModel("Rajasthan", "RJ", "","Rajasthan","state"),
  CityModel("Sikkim", "SK", "","Sikkim","state"),
  CityModel("Tamil nadu", "TN", "","Tamil nadu","state"),
  CityModel("Telangana", "TL", "","Telangana","state"),
  CityModel("Tripura", "TR", "","Tripura","state"),
  CityModel("Uttar pradesh", "UP", "","Uttar pradesh","state"),
  CityModel("Uttarakhand", "UK", "","Uttarakhand","state"),
  CityModel("West bengal", "WB", "","West bengal","state")
)

/**
 * Cluster Response
 */
data class ClusterResponse(
  @SerializedName("results") val clusters: List<Clusters>
)

data class Clusters(
  @SerializedName("cluster_id") val clusterId: String
)

const val CitySelected = "city_selected"