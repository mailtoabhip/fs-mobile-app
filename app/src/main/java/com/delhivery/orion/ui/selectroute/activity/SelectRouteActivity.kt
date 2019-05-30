package com.delhivery.orion.ui.selectroute.activity

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.delhivery.orion.R
import com.delhivery.orion.data.RouteModel
import com.delhivery.orion.databinding.ActivitySelectRouteBinding
import com.delhivery.orion.ui.base.BaseLocationActivity
import com.delhivery.orion.ui.home.HomeActivity
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType.AddNewRoute
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType.UserRoutes
import com.delhivery.orion.ui.selectroute.fragments.BaseSelectRouteFragmentAction
import com.delhivery.orion.ui.selectroute.fragments.DestinationSelectedAction
import com.delhivery.orion.ui.selectroute.fragments.OriginSelectedAction
import com.delhivery.orion.ui.selectroute.fragments.RouteDetailAction
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.AddMoreRoutes
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.DestinationsAdded
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.LoadRequests
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.OriginSelected
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentActionType.RouteDetail
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentType
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentType.DestinationFragment
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentType.OriginCityFragment
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentType.RouteDetailFragment
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentType.RouteListFragment
import com.delhivery.orion.ui.selectroute.fragments.destination.SelectRouteDestinationFragment
import com.delhivery.orion.ui.selectroute.fragments.detail.SelectRouteDetailFragment
import com.delhivery.orion.ui.selectroute.fragments.routeslist.SelectRouteListFragment
import com.delhivery.orion.utils.LocationFlowState

class SelectRouteActivity : BaseLocationActivity<ActivitySelectRouteBinding, SelectRouteViewModel>() {
  override fun getViewModelClass() = SelectRouteViewModel::class.java

  override fun layoutId() = R.layout.activity_select_route

  override fun requireConnection() = true

  /* current Fragment type */
  private var currentFragmentType: SelectRouteFragmentType? = null

  /* flow type */
  private var flowType: SelectRouteFlowType = AddNewRoute

  /* current route model */
  private var currentRoute: RouteModel? = null

  private var selectedRoute: RouteModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* flow type */
    flowType = intent?.getIntExtra(
        SelectRouteFlowTypeIntentExtra, AddNewRoute.typeId
    )
        ?.let {
          SelectRouteFlowType.byTypeId(it)
        } ?: AddNewRoute
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* start with origin city fragment */
    navigate(SelectRouteFragmentType.initFragment(flowType))

    //TODO: does this needs to be here or can we shift this to fragment since fragment should update its data itself not via activity

    /* observe routes and update route list fragment */
    viewModel.routesLiveData.observe(this, Observer {
      val _fragment = supportFragmentManager.findFragmentByTag(
          SelectRouteFragmentTag
      )
      if (_fragment is SelectRouteListFragment) {
        _fragment.routes = it?.toMutableList() ?: mutableListOf<RouteModel>()
        _fragment.addRoutes()
      }
    })

    when (flowType) {
      UserRoutes -> viewModel.fetchUserRoutes()
    }
  }

  /**
   * Navigate to [SelectRouteFragmentType] _fragment
   */
  private fun navigate(
    fragmentType: SelectRouteFragmentType
  ) {
    if (currentFragmentType == fragmentType) return
    currentFragmentType = fragmentType
    navigationUtils.addReplaceFragment(
        R.id.container, fragmentType.fragment,
        SelectRouteFragmentTag
    )
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
          currentRoute?.destinations = destinations.toMutableList()
          viewModel.addUserRoutes(currentRoute!!.expandNearByLocations()) { success ->
            if (success) {
              navigate(RouteListFragment)
            }
          }
          currentRoute = null
        }
      }
      AddMoreRoutes -> {
        navigate(OriginCityFragment)
      }
      LoadRequests -> {
        when (flowType) {
          AddNewRoute -> navigationUtils.navigate(HomeActivity::class.java, true)
          UserRoutes -> finish()
        }
      }
      RouteDetail -> {
        (action as RouteDetailAction).apply {
          selectedRoute = RouteModel(origin)
          selectedRoute?.destinations = destinations.toMutableList()
          navigate(RouteDetailFragment)
        }
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
      is SelectRouteDetailFragment -> {
        fragment.currentRoute = selectedRoute
      }
    }
  }

  override fun onBackPressed() {
    if (currentFragmentType?.prevFragment(flowType) != null) {
      navigate(currentFragmentType!!.prevFragment(flowType)!!)
    } else {
      super.onBackPressed()
    }
  }
}

/* Search load fragment tag */
private const val SelectRouteFragmentTag = "select_route_fragment_tag"

/* Flow type intent key */
private const val SelectRouteFlowTypeIntentExtra = "select_route_flow_type"

/**
 * Select route intent for [SelectRouteFlowType]
 */
fun selectRouteIntent(
  context: Context,
  type: SelectRouteFlowType
) = Intent(context, SelectRouteActivity::class.java).apply {
  putExtra(SelectRouteFlowTypeIntentExtra, type.typeId)
}