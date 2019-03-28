package com.delhivery.orion.data

/**
 * Route model
 */
data class RouteModel(
  var origin: CityModel,
  var nearByLocation: List<CityModel> = listOf(),
  var destinations: List<StateModel> = listOf()
) {
  fun destinationsCount() = "${destinations.size} States"
}