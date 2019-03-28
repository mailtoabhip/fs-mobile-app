package com.delhivery.orion.ui.selectroute

import android.os.Bundle
import android.support.v4.app.Fragment
import com.delhivery.orion.R
import com.delhivery.orion.data.RouteModel
import com.delhivery.orion.databinding.ActivitySelectRouteBinding
import com.delhivery.orion.ui.base.BaseLocationActivity
import com.delhivery.orion.ui.selectroute.fragments.BaseSelectRouteFragmentAction
import com.delhivery.orion.ui.selectroute.fragments.DestinationSelectedAction
import com.delhivery.orion.ui.selectroute.fragments.OriginSelectedAction
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.AddMoreRoutes
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.DestinationsAdded
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.LoadRequests
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.OriginSelected
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentType
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentType.DestinationFragment
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentType.OriginCityFragment
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentType.RouteListFragment
import com.delhivery.orion.ui.selectroute.fragments.destination.SelectRouteDestinationFragment
import com.delhivery.orion.ui.selectroute.fragments.routeslist.SelectRouteListFragment
import com.delhivery.orion.utils.LocationFlowState

class SelectRouteActivity : BaseLocationActivity<ActivitySelectRouteBinding, SelectRouteViewModel>() {
  override fun getViewModelClass() = SelectRouteViewModel::class.java

  override fun layoutId() = R.layout.activity_select_route

  override fun requireConnection() = true

  /* current Fragment type */
  private var currentFragmentType: SelectRouteFragmentType? = null

  /* current route model */
  private var currentRoute: RouteModel? = null

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* start with origin city fragment */
    navigate(OriginCityFragment)
  }

  /**
   * Navigate to [SelectRouteFragmentType] _fragment
   */
  private fun navigate(
    fragmentType: SelectRouteFragmentType,
    args: Any? = null
  ) {
    if (currentFragmentType == fragmentType) return
    supportFragmentManager.beginTransaction()
        .apply {
          val _fragment = supportFragmentManager.findFragmentByTag(SelectRouteFragmentTag)
          if (_fragment == null) {
            add(R.id.container, fragmentType.fragment, SelectRouteFragmentTag)
          } else {
            replace(R.id.container, fragmentType.fragment, SelectRouteFragmentTag)
          }
          currentFragmentType = fragmentType
        }
        .commitNow()
  }

  /**
   * Fragment action observer
   */
  fun fragmentAction(action: BaseSelectRouteFragmentAction) {
    when (action.type) {
      OriginSelected -> {
        (action as OriginSelectedAction).apply {
          currentRoute = RouteModel(origin, nearByLocations)
          navigate(DestinationFragment)
        }
      }
      DestinationsAdded -> {
        (action as DestinationSelectedAction).apply {
          currentRoute?.destinations = destinations
          viewModel.routes.add(currentRoute!!)
          currentRoute = null
          //navigate to routes fragment
          navigate(RouteListFragment)
        }
      }
      AddMoreRoutes -> {
        navigate(OriginCityFragment)
      }
      LoadRequests -> {
        //go to home
      }
    }
  }

  override fun updateLocationFlowState(flowState: LocationFlowState) {
//    //handling if needed here
  }

  override fun onAttachFragment(fragment: Fragment?) {
    super.onAttachFragment(fragment)
    when (fragment) {
      is SelectRouteDestinationFragment -> {
        fragment.originCity = currentRoute?.origin
      }
      is SelectRouteListFragment -> {
        fragment.routes = viewModel.routes
      }
    }
  }

  override fun onBackPressed() {
    if (currentFragmentType?.prevFragment() != null) {
      navigate(currentFragmentType!!.prevFragment()!!)
    } else {
      super.onBackPressed()
    }
  }
}

/* Search load fragment tag */
private const val SelectRouteFragmentTag = "select_route_fragment_tag"