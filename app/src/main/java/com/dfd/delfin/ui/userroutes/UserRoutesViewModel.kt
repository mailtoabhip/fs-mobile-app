package com.dfd.delfin.ui.userroutes

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.data.RouteMappingModel
import com.dfd.delfin.data.home.routes.RouteModel
import com.dfd.delfin.data.userroutes.UserRoutesWarningItem_NoMember
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Add
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.dfd.delfin.ui.dialogs.RouteDeleteDialogInterface
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 5/1/21
 */

class UserRoutesViewModel @Inject constructor(
  private val userRepository: UserRepository,
  val userPrefs: UserPrefs
): BaseViewModel(), RouteDeleteDialogInterface {

  var routesLiveData =
    MutableLiveData<Pair<Pair<String, String>, MutableList<RouteModel>>>()

  var allRoutesLiveData = MutableLiveData<List<Pair<BaseUserRouteRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* selected route models */
  var routes = mutableListOf<RouteModel>()
  var existingRoutes = mutableListOf<RouteModel>()
  var updatedLanes = MutableLiveData<Boolean>()
    var emptyState = MutableLiveData<Boolean>()

  /**
   * Fetch user routes
   */
  fun fetchUserRoutes() {

    Pair(UserRouteProgressItem(), AddUpdate).let { allRoutesLiveData.postValue(listOf(it)) }

    compositeDisposable += userRepository.getUser(false)
        .onBackground()
        .progress()
        .subscribe { _user, error ->
          if (!error) {
            mutableListOf<Pair<BaseUserRouteRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(UserRouteProgressItem(), Remove))

            if(_user.userRoutes().isNotEmpty()) {
                for (route in _user.userRoutes()) {
                    add(Pair(UserRouteItem(route), Add))
                }
                emptyState.value = false
            }
            else{
                add(Pair(UserRoutesWarningItem_NoMember, Add))
                emptyState.value = true
            }
            }.let {
              allRoutesLiveData.postValue(it)
            }
            routes = _user.userRoutes() as MutableList<RouteModel>
            routesLiveData.postValue(
                    Pair(Pair(_user.supplierDetails?.baseCity, _user.supplierDetails?.baseCityCode), routes) as Pair<Pair<String, String>, MutableList<RouteModel>>?
            )
          } else {
            mutableListOf<Pair<BaseUserRouteRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(UserRouteProgressItem(), Remove))
            }.let {
              allRoutesLiveData.postValue(it)
            }
          }
        }
  }

  override fun deleteRoute(route: RouteModel) {
      /**
       * this route is going to be deleted by overwriting the routes(in db) with existing routes(after removing this route)
       */
      val existingVendorRoutes = getLanePreferences(route)
  }

  private fun getLanePreferences(route: RouteModel): List<RouteMappingModel> {
        existingRoutes.clear()
        val deletedRoute = route.expandLocations()
        for (routeVal in routes) {
            for (newRoute in deletedRoute) {
                if (routeVal.origin.orion_db_city_code != newRoute.origin.orion_db_city_code) {
                    existingRoutes.add(routeVal)
                }
            }
        }

      return mutableListOf<RouteMappingModel>().apply {
          existingRoutes.forEach { addAll(it.toMapping()) }
      }
  }

}