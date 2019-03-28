package com.delhivery.orion.ui.selectroute.fragments

import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.StateModel
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.AddMoreRoutes
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.DestinationsAdded
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.LoadRequests
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.OriginSelected

/**
 * Select route fragment action type
 */
enum class SelectRouteFragmentActionType {
  OriginSelected,
  DestinationsAdded,
  AddMoreRoutes,
  LoadRequests
}

/**
 * Select route fragment action base class
 */
abstract class BaseSelectRouteFragmentAction(val type: SelectRouteFragmentActionType)

/**
 * Origin [CityModel] selected
 */
class OriginSelectedAction(
  val origin: CityModel,
  val nearByLocations: List<CityModel>
) : BaseSelectRouteFragmentAction(OriginSelected)

/**
 * Destination [StateModel] selected
 */
class DestinationSelectedAction(
  val destinations: List<StateModel>
) : BaseSelectRouteFragmentAction(DestinationsAdded)

/**
 * Add more routes [RouteModel]
 */
class AddMoreRoutesAction() : BaseSelectRouteFragmentAction(AddMoreRoutes)

/**
 * Go to load request/home
 */
class LoadRequestsAction() : BaseSelectRouteFragmentAction(LoadRequests)