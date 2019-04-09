package com.delhivery.orion.ui.selectroute

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.RouteMappingModel
import com.delhivery.orion.data.RouteModel
import com.delhivery.orion.repository.UserRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

class SelectRouteViewModel @Inject constructor(
  private val userRepository: UserRepository
) : BaseViewModel() {

  var routesLiveData = MutableLiveData<List<RouteModel>>()

  /* selected route models */
  var routes = mutableListOf<RouteModel>()

  /**
   * Fetch user routes
   */
  fun fetchUserRoutes() {
    compositeDisposable += userRepository.getUser()
        .onBackground()
        .progress()
        .subscribe { _user, error ->
          if (!error) {
            routes.addAll(_user.userRoutes())
            routesLiveData.postValue(routes)
          } else {
            error.printStackTrace()
          }
        }
  }

  /**
   * Add Routes
   */
  fun addUserRoutes(
    newRoutes: List<RouteModel>,
    completedAction: (success: Boolean) -> Unit
  ) {
    val _routeMappings = mutableListOf<RouteMappingModel>().apply {
      newRoutes.forEach { addAll(it.toMapping()) }
    }
    compositeDisposable += userRepository.addRoutes(_routeMappings)
        .onBackground()
        .progress()
        .subscribe { _routes, error ->
          if (!error) {
            routes.addAll(_routes)
            routesLiveData.postValue(routes)
            completedAction(true)
          } else {
            error.printStackTrace()
            completedAction(false)
          }
        }
  }
}