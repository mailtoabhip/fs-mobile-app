package com.delhivery.axle.ui.selectroute.activity

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.RouteMappingModel
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class SelectRouteViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var routesLiveData = MutableLiveData<Triple<String, String, MutableList<RouteModel>>>()

  /* selected route models */
  var routes = mutableListOf<RouteModel>()

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
            routesLiveData.postValue(Triple(_user.baseCity, _user.baseCityCode, routes))
          } else {
            error.handle()
          }
        }
  }

  /**
   * Update Base city and Routes
   */
  fun updateBaseCityAndRoutes(
    city: CityModel?,
    newRoutes: List<RouteModel>,
    completedAction: (success: Boolean) -> Unit
  ) {
    val _routeMappings = mutableListOf<RouteMappingModel>().apply {
      newRoutes.forEach { addAll(it.toMapping()) }
    }
    compositeDisposable += userRepository.updateBaseCityAndRoutes(city, _routeMappings)
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
   * Update Routes
   */
  fun updateUserRoutes(
    newRoutes: List<RouteModel>,
    completedAction: (success: Boolean) -> Unit
  ) {
    val _routeMappings = mutableListOf<RouteMappingModel>().apply {
      newRoutes.forEach { addAll(it.toMapping()) }
    }
    compositeDisposable += userRepository.updateUserRoutes(_routeMappings)
        .onBackground()
        .progress()
        .subscribe { _routes, error ->
          if (!error) {
            userPrefs.routeUpdate = true
            completedAction(true)
          } else {
            error.handle()
            completedAction(false)
          }
        }
  }

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
}