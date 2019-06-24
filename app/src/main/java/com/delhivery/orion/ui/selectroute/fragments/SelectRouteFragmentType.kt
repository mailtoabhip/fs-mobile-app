package com.delhivery.orion.ui.selectroute.fragments

import com.delhivery.orion.ui.selectroute.SelectRouteFlowType
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType.AddNewRoute
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.orion.ui.selectroute.fragments.destination.SelectRouteDestinationFragment
import com.delhivery.orion.ui.selectroute.fragments.detail.SelectRouteDetailFragment
import com.delhivery.orion.ui.selectroute.fragments.origincity.SelectRouteOriginCityFragment
import com.delhivery.orion.ui.selectroute.fragments.routeslist.SelectRouteListFragment

/**
 * Select route fragment type
 */
enum class SelectRouteFragmentType(
  val step: Int,
  val fragment: SelectRouteBaseFragment<*, *>,
  val title: String? = null
) {
  OriginCityFragment(0, SelectRouteOriginCityFragment._instance),
  DestinationFragment(1, SelectRouteDestinationFragment._instance),
  RouteListFragment(2, SelectRouteListFragment._instance),
  RouteDetailFragment(3, SelectRouteDetailFragment._instance);

  /**
   * Previous fragment on back pressed
   */
  fun prevFragment(type: SelectRouteFlowType) = when (type) {
    AddNewRoute -> when (step) {
      1, 2 -> OriginCityFragment
      else -> null
    }

    EditRoute -> null
  }

  companion object {

    /**
     * Init fragment based on [SelectRouteFlowType]
     */
    fun initFragment(type: SelectRouteFlowType) = when (type) {
      AddNewRoute -> OriginCityFragment
      EditRoute -> RouteDetailFragment
    }
  }
}