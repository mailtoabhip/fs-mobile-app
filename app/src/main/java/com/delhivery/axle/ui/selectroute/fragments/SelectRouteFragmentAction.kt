package com.delhivery.axle.ui.selectroute.fragments

import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.AddMoreRoutes
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.DestinationsAdded
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.EditOrigin
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.LoadRequests
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.OriginSelected
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.RouteDetail
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.RouteUpdate

/**
 * Select route fragment action type
 */
enum class SelectRouteFragmentActionType {
  OriginSelected,
  DestinationsAdded,
  AddMoreRoutes,
  LoadRequests,
  RouteDetail,
  RouteUpdate,
  EditOrigin
}

/**
 * Select route fragment action base class
 */
abstract class BaseSelectRouteFragmentAction(val type: SelectRouteFragmentActionType)

/**
 * Origin [CityModel] selected
 */
class OriginSelectedAction(
  val origin: CityModel
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
class AddMoreRoutesAction : BaseSelectRouteFragmentAction(AddMoreRoutes)

/**
 * @Deprecated
 * Navigate to route detail page
 */
class RouteDetailAction(
  val route: RouteModel
) : BaseSelectRouteFragmentAction(RouteDetail)

/**
 * @NotUsed
 * Go to load request/home
 */
class LoadRequestsAction : BaseSelectRouteFragmentAction(LoadRequests)

/**
 * Update Current route
 */
class RouteUpdateAction(
  val route: RouteModel
) : BaseSelectRouteFragmentAction(RouteUpdate)

/**
 * Update Current route
 */
class RouteEditOriginAction(
  val route: RouteModel
) : BaseSelectRouteFragmentAction(EditOrigin)