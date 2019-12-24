package com.delhivery.axle.ui.selectroute.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.databinding.ActivitySelectRouteBinding
import com.delhivery.axle.ui.base.BaseLocationActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.AddNewRoute
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.axle.ui.selectroute.fragments.BaseSelectRouteFragmentAction
import com.delhivery.axle.ui.selectroute.fragments.DestinationSelectedAction
import com.delhivery.axle.ui.selectroute.fragments.OriginSelectedAction
import com.delhivery.axle.ui.selectroute.fragments.RouteDetailAction
import com.delhivery.axle.ui.selectroute.fragments.RouteEditOriginAction
import com.delhivery.axle.ui.selectroute.fragments.RouteUpdateAction
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.AddMoreRoutes
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.DestinationsAdded
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.EditOrigin
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.LoadRequests
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.OriginSelected
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.RouteDetail
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentActionType.RouteUpdate
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentType
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentType.DestinationFragment
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentType.OriginCityFragment
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentType.RouteDetailFragment
import com.delhivery.axle.ui.selectroute.fragments.destination.SelectRouteDestinationFragment
import com.delhivery.axle.ui.selectroute.fragments.detail.SelectRouteDetailFragment
import com.delhivery.axle.ui.selectroute.fragments.routeslist.SelectRouteListFragment
import com.delhivery.axle.utils.LocationFlowState
import com.google.android.material.snackbar.Snackbar

/**
 * Handles route updation
 */
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

  private var addRouteOnLogin: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    addRouteOnLogin = intent?.extras?.getBoolean(SelectRouteWelcomeIntentExtra) ?: false

    /* flow type */
    try {
      flowType = intent?.getIntExtra(
          SelectRouteFlowTypeIntentExtra, AddNewRoute.typeId
      )?.let { SelectRouteFlowType.byTypeId(it) } ?: AddNewRoute
    } catch (e: Exception) {
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* start with origin city fragment */
    navigate(SelectRouteFragmentType.initFragment(flowType))

    /* observe routes and update route list fragment */
    viewModel.routesLiveData.observe(this, Observer {
      val _fragment = supportFragmentManager.findFragmentByTag(SelectRouteFragmentTag)
      if (_fragment is SelectRouteDetailFragment && it != null) {

        if (!it.third.isNullOrEmpty()) {
          val routeModel = it.third.get(0)
          currentRoute = RouteModel(CityModel(routeModel.origin.city, routeModel.origin.cityId))
          currentRoute?.destinations = it.third[0].destinations
        } else {
          currentRoute = RouteModel(CityModel(it.first, it.second))
        }
        _fragment.route = currentRoute
        _fragment.populateRoute()
      }
    })

    if (flowType == EditRoute) viewModel.fetchUserRoutes()
  }

  /**
   * Navigate to [SelectRouteFragmentType] _fragment
   */
  private fun navigate(
    fragmentType: SelectRouteFragmentType
  ) {
    if (currentFragmentType == fragmentType) return
    currentFragmentType = fragmentType
    navigationUtils.replaceFragment(
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
          when (currentRoute) {
            null -> {
              currentRoute = RouteModel(origin)
            }
            else -> {
              currentRoute?.origin = origin
            }
          }
          navigate(DestinationFragment)
        }
      }
      DestinationsAdded -> {
        (action as DestinationSelectedAction).apply {
          currentRoute?.destinations = destinations.toMutableSet()
          viewModel.updateUserRoutes(
              currentRoute!!.expandLocations()
          ) { routeUpdateSuccess ->
            if (routeUpdateSuccess) {
              viewModel.fetchUser { userUpdateSuccess ->
                when (userUpdateSuccess) {
                  true -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                  }
                  else -> {
                    uiUtils.showSnackbar(
                        "Routes addition failed, Try again!", Snackbar.LENGTH_LONG
                    )
                  }
                }
              }
            } else {
              uiUtils.showSnackbar(
                  "Routes addition failed, Try again!", Snackbar.LENGTH_LONG
              )
            }
          }
        }
      }
      AddMoreRoutes -> {
        navigate(OriginCityFragment)
      }
      LoadRequests -> {
        when (flowType) {
          AddNewRoute -> navigationUtils.navigate(
              HomeActivity::class.java, true
          )
          EditRoute -> finish()
        }
      }
      RouteDetail -> {
        (action as RouteDetailAction).apply {
          navigate(RouteDetailFragment)
        }
      }
      RouteUpdate -> {
        (action as RouteUpdateAction).apply {
          val _routes = mutableListOf<RouteModel>().apply {
            add(route)
          }

          viewModel.updateUserRoutes(_routes) { _success ->
            viewModel.setRoutesUpdated(route)
            finish()
          }
        }
      }
      EditOrigin -> {
        (action as RouteEditOriginAction).apply {
          currentRoute = route
          viewModel.setRoutesUpdated(route)
          navigate(OriginCityFragment)
        }
      }
    }
  }

  override fun updateLocationFlowState(flowState: LocationFlowState) {
//    //handling if needed here
  }

  override fun onAttachFragment(fragment: Fragment) {
    super.onAttachFragment(fragment)
    when (fragment) {
      is SelectRouteDestinationFragment -> {
        fragment.originCity = currentRoute?.origin
      }
      is SelectRouteListFragment -> {
        fragment.routes = viewModel.routes
      }
      is SelectRouteDetailFragment -> {
        if (currentRoute != null) {
          fragment.route = currentRoute
        } else if (viewModel.routes.size > 0) {
          fragment.route = viewModel.routes.get(0)
        }
      }
    }
  }

  override fun onBackPressed() {
    if (currentFragmentType?.prevFragment(flowType) != null && !addRouteOnLogin) {
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

/* Navigate from [SelectRouteWelcomeActivity] */
const val SelectRouteWelcomeIntentExtra = "select_route_welcome_tag"

/**
 * Select route intent for [SelectRouteFlowType]
 */
fun selectRouteIntent(
  context: Context,
  type: SelectRouteFlowType = AddNewRoute
): Intent = Intent(context, SelectRouteActivity::class.java).apply {
  putExtra(SelectRouteFlowTypeIntentExtra, type.typeId)
}