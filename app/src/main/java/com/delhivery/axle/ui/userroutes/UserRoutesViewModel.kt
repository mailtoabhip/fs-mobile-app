package com.delhivery.axle.ui.userroutes

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.DeleteRouteRequest
import com.delhivery.axle.data.RouteMappingModel
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.dialogs.RouteDeleteDialogInterface
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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

              for (route in _user.userRoutes()) {
                add(Pair(UserRouteItem(route), Add))
              }
            }.let {
              allRoutesLiveData.postValue(it)
            }
            routes = _user.userRoutes() as MutableList<RouteModel>
            routesLiveData.postValue(
                Pair(Pair(_user.baseCity, _user.baseCityCode), routes)
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
      val jsonArray = getLanePreferences(route)
      val deleteRouteRequest = DeleteRouteRequest(userPrefs.userName , jsonArray , userPrefs.vendorEntity,"axle-app")
      compositeDisposable += userRepository.deleteUserRoutes(deleteRouteRequest)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if(!error){
            updatedLanes.postValue(true)
          }
          else{
            updatedLanes.postValue(false)
            error.handle()
          }
        }
  }

  private fun getLanePreferences(route: RouteModel): JsonArray {
        existingRoutes.clear()
        val newRoutes = route.expandLocations()
        for (routeVal in routes) {
            for (newRoute in newRoutes) {
                if (routeVal.origin.orion_db_city_code != newRoute.origin.orion_db_city_code) {
                    existingRoutes.add(routeVal)
                }
            }
        }
        val routeMappings = mutableListOf<RouteMappingModel>().apply {
            existingRoutes.forEach { addAll(it.toMapping()) }
        }

        val jsonArray = JsonArray()
        routeMappings.forEach {
            val json = JsonObject()
            val originJson = JsonObject()
            it.origin.city.let { it1 -> originJson.addProperty("city", it1) }
            it.origin.orion_db_city_code?.let { it1 -> originJson.addProperty("city_id", it1) }
            json.add("origin", originJson)

            val destinationJson = JsonObject()
            it.destination.state.let { it1 -> destinationJson.addProperty("state", it1) }
            it.destination.stateId.let { it1 -> destinationJson.addProperty("state_id", it1) }
            json.add("destination", destinationJson)
            jsonArray.add(json)
        }
        return jsonArray
  }

}