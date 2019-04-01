package com.delhivery.orion.data

/**
 * Route model
 */
data class RouteModel(
  var origin: CityModel,
  var nearByLocation: List<CityModel> = listOf(),
  var destinations: MutableList<StateModel> = mutableListOf()
) {
  fun destinationsCount() = "${destinations.size} States"

  /**
   * Source/Destination route mapping / list of [RouteMappingModel]
   */
  fun toMapping() = mutableListOf<RouteMappingModel>().apply {
    destinations.forEach { _destination ->
      add(RouteMappingModel(origin, _destination))
    }
  }

  /**
   * Expand near by loactions also to list of [RouteModel]
   */
  fun expandNearByLocations(): List<RouteModel> = mutableListOf<RouteModel>().apply {
    add(RouteModel(origin, destinations = destinations))
    nearByLocation.forEach {
      add(RouteModel(it, destinations = destinations))
    }
  }
}

/**
 * Expand routes from nearby locations
 */
fun List<RouteModel>.expandRoutes(): List<RouteModel> {
  val routes = mutableMapOf<String, RouteModel>()
  map { route ->
    for (i in -1 until route.nearByLocation.size) {
      when (i) {
        -1 -> route.origin
        else -> route.nearByLocation[i]
      }.let { _origin ->
        if (!routes.containsKey(_origin.key()) || routes[_origin.key()] == null) {
          routes[_origin.key()] = RouteModel(_origin)
        }
        routes[_origin.key()]!!.destinations = route.destinations
      }
    }
  }
  return routes.values.toList()
}