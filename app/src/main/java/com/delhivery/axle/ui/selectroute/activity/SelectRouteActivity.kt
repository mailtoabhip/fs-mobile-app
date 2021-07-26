package com.delhivery.axle.ui.selectroute.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.UserCity
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
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

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

  private var allRoutes: List<RouteModel> = mutableListOf()

  private var addRouteOnLogin: Boolean = false

  private var originCityCode: String = ""

  @Inject lateinit var userPrefs: UserPrefs

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    addRouteOnLogin = intent?.extras?.getBoolean(SelectRouteWelcomeIntentExtra) ?: false

    originCityCode = intent?.extras?.getString(SelectRouteOriginCityExtra, "")
        .toString()

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

    viewModel.allRoutesLiveData.observe(this, Observer {
      if (!it.second.isNullOrEmpty()) {
        allRoutes = it.second
      }
    })

    /* observe routes and update route list fragment */
    viewModel.routesLiveData.observe(this, Observer {
      val _fragment = supportFragmentManager.findFragmentByTag(SelectRouteFragmentTag)
      if (_fragment is SelectRouteDetailFragment && it != null) {

        if (!it.second.isNullOrEmpty()) {
          for (route in it.second) {
            if (route.origin.orion_db_city_code == originCityCode) {
              currentRoute = RouteModel(route.origin)
              currentRoute?.destinations = route.destinations
              break
            }
          }
        } else {
          currentRoute = RouteModel(UserCity(it.first.first, it.first.second))
        }
        _fragment.route = currentRoute
        _fragment.populateRoute()
      }
    })

    viewModel.fetchUserRoutes()
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
              currentRoute = RouteModel(origin.getUserCity())
            }
            else -> {
              currentRoute?.origin = origin.getUserCity()
            }
          }
          if(userPrefs.firstRoute){
            userPrefs.firstRoute = false
            analyticsUtil.trackEvent(
                    EVENT_ENTER_FIRST_OC,
                    mutableListOf( PROPERTY_USER_ID , PROPERTY_ORIGIN_CITY_CAPTURED),
                    mutableListOf( userPrefs.userId(), currentRoute!!.origin.city)
            )
          }
          navigate(DestinationFragment)
        }
      }
      DestinationsAdded -> {
        (action as DestinationSelectedAction).apply {
          currentRoute?.destinations = destinations.toMutableSet()
          viewModel.updateUserRoutes(
              currentRoute!!.expandLocations(), allRoutes
          ) { routeUpdateSuccess ->
            if (routeUpdateSuccess) {
              viewModel.fetchUser { userUpdateSuccess ->
                when (userUpdateSuccess) {
                  true -> {
                    analyticsUtil.trackEvent(
                            EVENT_EDIT_PREFERENCES,
                            mutableListOf(PROPERTY_USER_ID , PROPERTY_ATTRIBUTE_CHANGED),
                            mutableListOf(userPrefs.userId()  )
                    )
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
          AddNewRoute -> {
            val states  = mutableListOf<String>()
            for (destination in currentRoute!!.destinations){
              states.add(destination.state)
            }
            analyticsUtil.trackEvent(
                    EVENT_CONFIRM_FIRST_ROUTE,
                    mutableListOf(PROPERTY_USER_ID , PROPERTY_ROUTE_PREFERENCES),
                    mutableListOf(userPrefs.userId() , currentRoute!!.origin.city + "to " + states.toString())
            )
            navigationUtils.navigate(
                    HomeActivity::class.java, true
            )
          }
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

          viewModel.updateUserRoutes(_routes, allRoutes) { _success ->
            viewModel.setRoutesUpdated()
            finish()
          }
        }
      }
      EditOrigin -> {
        (action as RouteEditOriginAction).apply {
          currentRoute = route
          viewModel.setRoutesUpdated()
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
          fragment.route = viewModel.routes[0]
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

/* Select route basis origin city key */
const val SelectRouteOriginCityExtra = "select_route_origin_city_code"

/**
 * Select route intent for [SelectRouteFlowType]
 */
fun selectRouteIntent(
  context: Context,
  type: SelectRouteFlowType = AddNewRoute
): Intent = Intent(context, SelectRouteActivity::class.java).apply {
  putExtra(SelectRouteFlowTypeIntentExtra, type.typeId)
}