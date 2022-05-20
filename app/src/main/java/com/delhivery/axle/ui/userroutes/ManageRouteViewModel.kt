package com.delhivery.axle.ui.userroutes

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.OriginDestinations
import com.delhivery.axle.api.request.RouteDetails
import com.delhivery.axle.api.request.UpdateRouteRequest
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.RouteMappingModel
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.data.UserCity
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.searchcitystate.selectedCityStates
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class ManageRouteViewModel@Inject constructor(
  private val userRepository: UserRepository,
  private val loadboardRepository: LoadboardRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var userAddRouteLiveData = MutableLiveData<Boolean>()
  var selectedOrigin: CityModel? = null
  var oldOrigin: CityModel? = null
  var destinationsPreferenceList = ArrayList<RouteDetails>()
  private var originRoute:RouteDetails?=null
  private var oldOriginRoute:RouteDetails?=null

  fun addUserRouteDetails() {
    if (!isConnected) return

    for (item in selectedCityStates) {
      if (selectedOrigin != null) {
        originRoute = RouteDetails(
          selectedOrigin!!.orionDbCityCode,
          selectedOrigin!!.cityName(),
          "city"
        )
        val destinationsRoute = RouteDetails(item.orionDbCityCode, item.cityName(), if(item.type=="state")"state" else "city")
        destinationsPreferenceList.add(destinationsRoute)
      }
    }
    compositeDisposable += loadboardRepository.addRouteDetails(
      userRepository.userId(),
      UpdateRouteRequest(
        origin = originRoute!!,
        destination = destinationsPreferenceList,
        oldOrigin = null
      )
    )
      .onBackground()
      .progress()
      .subscribe { _res, error ->
        if (!error) {
          userAddRouteLiveData.postValue(true)

        } else {
          error.handle()
          userAddRouteLiveData.postValue(false)
        }
      }

  }

  fun editUserRouteDetails() {
    if (!isConnected) return

    for (item in selectedCityStates) {
      if (selectedOrigin != null) {
        originRoute = RouteDetails(
          selectedOrigin!!.orionDbCityCode,
          selectedOrigin!!.cityName(),
          "city"
        )
        oldOriginRoute = RouteDetails(
          oldOrigin!!.orionDbCityCode,
          oldOrigin!!.cityName(),
          "city"
        )
        val destinationsRoute = RouteDetails(item.orionDbCityCode, item.cityName(), if(item.type=="state")"state" else "city")
        destinationsPreferenceList.add(destinationsRoute)
      }
    }
    compositeDisposable += loadboardRepository.editRouteDetails(
      userRepository.userId(),
      UpdateRouteRequest(
        origin = originRoute!!,
        destination = destinationsPreferenceList,
        oldOrigin = oldOriginRoute
      )
    )
      .onBackground()
      .progress()
      .subscribe { _res, error ->
        if (!error) {
          userAddRouteLiveData.postValue(true)

        } else {
          error.handle()
          userAddRouteLiveData.postValue(false)
        }
      }

  }

  fun deleteUserRouteDetails() {
    if (!isConnected) return

    for (item in selectedCityStates) {
      if (selectedOrigin != null) {
        originRoute = RouteDetails(
          selectedOrigin!!.orionDbCityCode,
          selectedOrigin!!.cityName(),
          "city"
        )
        val destinationsRoute = RouteDetails(item.orionDbCityCode, item.cityName(), if(item.type=="state")"state" else "city")
        destinationsPreferenceList.add(destinationsRoute)
      }
    }
    compositeDisposable += loadboardRepository.deleteRouteDetails(
      userRepository.userId(),
      UpdateRouteRequest(
        origin = originRoute!!,
        destination = destinationsPreferenceList,
        oldOrigin = null
      )
    )
      .onBackground()
      .progress()
      .subscribe { _res, error ->
        if (!error) {
          userAddRouteLiveData.postValue(true)

        } else {
          error.handle()
          userAddRouteLiveData.postValue(false)
        }
      }

  }

}