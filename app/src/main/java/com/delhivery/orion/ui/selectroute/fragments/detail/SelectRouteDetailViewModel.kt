package com.delhivery.orion.ui.selectroute.fragments.detail

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.data.RouteMappingModel
import com.delhivery.orion.data.home.routes.RouteModel
import com.delhivery.orion.repository.UserRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import javax.inject.Inject

/**
 * Created by saurabh on 27,May,2019
 * for Delhivery Private Limited
 **
 * View model for [SelectRouteDetailFragment], handles routes edit
 **
 */
class SelectRouteDetailViewModel @Inject constructor(
  private val userRepository: UserRepository
) : BaseViewModel() {

  var routesLiveData = MutableLiveData<List<RouteModel>>()

  /* selected route models */
  var routes = mutableListOf<RouteModel>()

  fun fetchUserRoutes() {
    compositeDisposable += userRepository.getUser()
        .onBackground()
        .progress()
        .subscribe { _user, error ->
          if (!error) {
            routes.clear()
            routes.addAll(_user.userRoutes())
            routesLiveData.postValue(routes)
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
    completedAction: (success: Boolean) -> Unit
  ) {
    val _routeMappings = mutableListOf<RouteMappingModel>().apply {
      newRoutes.forEach { addAll(it.toMapping()) }
    }
    compositeDisposable += userRepository.updateRoutes(_routeMappings)
        .onBackground()
        .progress()
        .subscribe { _routes, error ->
          if (!error) {
            routes.clear()
            routes.addAll(_routes)
            routesLiveData.postValue(routes)
            completedAction(true)
          } else {
            error.handle()
            completedAction(false)
          }
        }
  }

}