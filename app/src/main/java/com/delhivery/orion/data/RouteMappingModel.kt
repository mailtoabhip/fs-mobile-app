package com.delhivery.orion.data

import com.google.gson.annotations.SerializedName

/**
 * Route mapping model
 */
data class RouteMappingModel(
  @SerializedName("origin") val origin: CityModel,
  @SerializedName("destination") val destination: StateModel
)

/**
 * Convert to route list
 */
fun List<RouteMappingModel>.toRoutes(): List<RouteModel> {
  val routes = mutableMapOf<String, RouteModel>()
  map {
    if (!routes.containsKey(it.origin.key()) || routes[it.origin.key()] == null) {
      routes[it.origin.key()] = RouteModel(it.origin)
    }
    routes[it.origin.key()]!!.destinations.add(it.destination)
  }
  return routes.values.toList()
}