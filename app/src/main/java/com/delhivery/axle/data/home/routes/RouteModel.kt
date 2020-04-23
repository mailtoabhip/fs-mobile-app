package com.delhivery.axle.data.home.routes

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.RouteMappingModel
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.data.UserCity

/**
 * Route model
 */
data class RouteModel(
  var origin: UserCity,
  var destinations: MutableSet<StateModel> = mutableSetOf()
) : BaseKeyTypeModel<String>() {

  override fun key() = origin.city

  fun statesCount() = "${destinations.size} States"

  fun destinationCount() = "${destinations.size}"

  /**
   * Source/Destination route mapping / list of [RouteMappingModel]
   */
  fun toMapping() = mutableSetOf<RouteMappingModel>().apply {
    destinations.forEach { _destination ->
      add(RouteMappingModel(origin, _destination))
    }
  }

  /**
   * Add origin destination to list of [RouteModel]
   */
  fun expandLocations(): List<RouteModel> = mutableListOf<RouteModel>().apply {
    add(RouteModel(origin, destinations))
  }
}

/**
 * Expand routes from nearby locations
 */
//fun List<RouteModel>.expandRoutes(): List<RouteModel> {
//  val routes = mutableMapOf<String, RouteModel>()
//  map { route ->
//    for (i in -1 until route.nearByLocation.size) {
//      when (i) {
//        -1 -> route.origin
//        else -> route.nearByLocation[i]
//      }.let { _origin ->
//        if (!routes.containsKey(_origin.key()) || routes[_origin.key()] == null) {
//          routes[_origin.key()] = RouteModel(_origin)
//        }
//        routes[_origin.key()]!!.destinations = route.destinations
//      }
//    }
//  }
//  return routes.values.toList()
//}

/* actions */
const val RoutesAction_ViewDetails = "routes_detail"