package com.delhivery.orion.ui.selectroute.fragments

import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.StateModel
import com.delhivery.orion.data.home.routes.RouteModel
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.AddMoreRoutes
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.DestinationsAdded
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.LoadRequests
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.OriginSelected
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.RouteDelete
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.RouteDetail
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.RouteUpdate

/**
 * Select route fragment action type
 */
enum class SelectRouteFragmentActionType {
  OriginSelected,
  DestinationsAdded,
  AddMoreRoutes,
  LoadRequests,
  RouteDetail,
  RouteDelete,
  RouteUpdate
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
 * Navigate to route detail page
 */
class RouteDetailAction(
  val route: RouteModel
) : BaseSelectRouteFragmentAction(RouteDetail)

/**
 * Delete route and navigate to Route List
 */
class RouteDeleteAction() : BaseSelectRouteFragmentAction(RouteDelete)

/**
 * Go to load request/home
 */
class LoadRequestsAction() : BaseSelectRouteFragmentAction(LoadRequests)

/**
 * Update Current route
 */
class RouteUpdateAction(
  val destinations: List<StateModel>
) : BaseSelectRouteFragmentAction(RouteUpdate)