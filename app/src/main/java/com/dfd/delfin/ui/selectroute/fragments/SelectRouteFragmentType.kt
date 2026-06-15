package com.dfd.delfin.ui.selectroute.fragments

import com.dfd.delfin.ui.selectroute.SelectRouteFlowType
import com.dfd.delfin.ui.selectroute.SelectRouteFlowType.AddNewRoute
import com.dfd.delfin.ui.selectroute.SelectRouteFlowType.DeleteRoute
import com.dfd.delfin.ui.selectroute.SelectRouteFlowType.EditRoute
import com.dfd.delfin.ui.selectroute.fragments.destination.SelectRouteDestinationFragment
import com.dfd.delfin.ui.selectroute.fragments.detail.SelectRouteDetailFragment
import com.dfd.delfin.ui.selectroute.fragments.origincity.SelectRouteOriginCityFragment

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
  RouteDetailFragment(2, SelectRouteDetailFragment._instance);

  /**
   * Previous fragment on back pressed
   */
  fun prevFragment(type: SelectRouteFlowType) = when (type) {
    AddNewRoute -> when (step) {
      1 -> OriginCityFragment
      else -> null
    }

    EditRoute -> null
    DeleteRoute -> null
  }

  companion object {

    /**
     * Init fragment based on [SelectRouteFlowType]
     */
    fun initFragment(type: SelectRouteFlowType) = when (type) {
      AddNewRoute -> OriginCityFragment
      EditRoute -> RouteDetailFragment
      DeleteRoute ->RouteDetailFragment
    }
  }
}