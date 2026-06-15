package com.dfd.delfin.ui.selectroute.activity

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.data.RouteMappingModel
import com.dfd.delfin.data.home.routes.RouteModel
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
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
                    Pair(Pair(_user.supplierDetails?.baseCity, _user.supplierDetails?.baseCityCode), routes) as Pair<Pair<String, String>, MutableList<RouteModel>>?
            )
            allRoutesLiveData.postValue(
                    Pair(Pair(_user.supplierDetails?.baseCity, _user.supplierDetails?.baseCityCode), routes) as Pair<Pair<String, String>, MutableList<RouteModel>>?
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
  fun setRoutesUpdated() {
    userPrefs.routeUpdate = true
  }
}