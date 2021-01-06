package com.delhivery.axle.ui.selectroute.activity

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.data.RouteMappingModel
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import okhttp3.Route
import javax.inject.Inject

/**
 * View model for [SelectRouteActivity]
 */
class SelectRouteViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var routesLiveData =
    MutableLiveData<Pair<Pair<String, String>, MutableList<RouteModel>>>()

  var allRoutesLiveData =
    MutableLiveData<Pair<Pair<String, String>, MutableList<RouteModel>>>()

  /* selected route models */
  var routes = mutableListOf<RouteModel>()

  var existingRoutes = mutableListOf<RouteModel>()

  /**
   * Fetch user routes
   */
  fun fetchUserRoutes() {
    compositeDisposable += userRepository.getUser(false)
        .onBackground()
        .progress()
        .subscribe { _user, error ->
          if (!error) {
            routes.addAll(_user.userRoutes())
            routesLiveData.postValue(
                Pair(Pair(_user.baseCity, _user.baseCityCode), routes)
            )
            allRoutesLiveData.postValue(
                Pair(Pair(_user.baseCity, _user.baseCityCode), routes)
            )
          } else {
            error.handle()
          }
        }
  }

  /**
   * Update Routes
   */
  fun updateUserRoutes(
    newRoutes: List<RouteModel>,
    allRoutes: List<RouteModel>,
    completedAction: (success: Boolean) -> Unit
  ) {

    for (route in allRoutes) {
      for (newRoute in newRoutes) {
        if (route.origin.orion_db_city_code != newRoute.origin.orion_db_city_code) {
          existingRoutes.add(route)
        }
      }
    }
    val routeMappings = mutableListOf<RouteMappingModel>().apply {
      newRoutes.forEach { addAll(it.toMapping()) }
      existingRoutes.forEach { addAll(it.toMapping()) }
    }
    compositeDisposable += userRepository.updateUserRoutes(routeMappings)
        .onBackground()
        .progress()
        .subscribe { _routes, error ->
          if (!error) {
            completedAction(true)
          } else {
            error.handle()
            completedAction(false)
          }
        }
  }

  /**
   * Fetch latest user data
   */
  fun fetchUser(completedAction: (success: Boolean) -> Unit) {
    compositeDisposable += userRepository.getUser(false)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            completedAction(true)
          } else {
            error.handle()
            completedAction(false)
          }
        }
  }

  /**
   * Set route updated flag
   */
  fun setRoutesUpdated(route: RouteModel) {
    userPrefs.routeUpdate = true
    //userPrefs.cityCode = route.origin.orion_db_city_code
  }
}