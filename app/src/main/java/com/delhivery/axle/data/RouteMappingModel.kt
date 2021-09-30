package com.delhivery.axle.data

import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName

/**
 * Route mapping model
 */
data class RouteMappingModel(
  @SerializedName("origin") val origin: UserCity,
  @SerializedName("destination") val destination: StateModel
)

/**
 * Convert to route list
 */
fun List<RouteMappingModel>.toRoutes(): MutableList<RouteModel> {
  val routes = mutableMapOf<String, RouteModel>()
  map {
    if (!routes.containsKey(it.origin.key()) || routes[it.origin.key()] == null) {
      routes[it.origin.key()] = RouteModel(it.origin)
    }
    routes[it.origin.key()]!!.destinations.add(it.destination)
  }
  return routes.values.toMutableList()
}

data class UserCity(
  @SerializedName("city") val city: String,
  @SerializedName("city_id") val orion_db_city_code: String? = ""
) : BaseKeyTypeModel<String>() {
  override fun key() = orion_db_city_code ?: ""

  fun cityName() = StringUtils.capitalize(city) ?: ""
}
