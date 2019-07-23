package com.delhivery.axle.ui.selectroute.fragments.origincity

import com.delhivery.axle.data.CityModel

/**
 * Base event for [SelectRouteOriginCityFragment]
 */
abstract class SelectRouteOriginCityBaseEvent

/**
 * Nearby locations
 */
data class SelectRouteOriginCityNearbyLocations(
  val originLocation: CityModel,
  val locations: List<CityModel>
) : SelectRouteOriginCityBaseEvent()

/**
 * Nearby locations
 */
data class SelectRouteOriginCitySelected(
  val originLocation: CityModel
) : SelectRouteOriginCityBaseEvent()

/**
 * Error event
 */
data class SelectRouteOriginCityErrorEvent(val message: String = "Error fetching nearby locations") :
    SelectRouteOriginCityBaseEvent()