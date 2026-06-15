package com.dfd.delfin.ui.onboarding

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.api.request.OriginDestinations
import com.dfd.delfin.api.request.RouteDetails
import com.dfd.delfin.api.request.UpdateUserRequest
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.data.RouteMappingModel
import com.dfd.delfin.data.StateModel
import com.dfd.delfin.data.UserCity
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.searchcitystate.selectedCityStates
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

class BasicDetailsViewModel @Inject constructor(
  private val loadboardRepository: LoadboardRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var userUpdateLiveData = MutableLiveData<Boolean>()
  var selectedOrigin: CityModel? = null
  var selectedTrucks = ArrayList<String>()
  var lanePreferenceList = ArrayList<OriginDestinations>()
  var vendorType: String? = null
  var routeType: String? = null

  fun updateUserDetails() {
    if (!isConnected) return

    for (item in selectedCityStates) {
      if (selectedOrigin != null) {
        val originRoute = RouteDetails(
          selectedOrigin!!.orionDbCityCode,
          selectedOrigin!!.cityName(),
           "city"
        )
        val destinationsRoute = RouteDetails(item.orionDbCityCode, item.cityName(), if(item.type=="state")"state" else "city")
        val originDestinations =
          OriginDestinations(origin = originRoute, destination = destinationsRoute)
        lanePreferenceList.add(originDestinations)
      }
    }
    
    // Map vendor type to API format
    val vendorTypeValue = when (vendorType) {
      "fleet_owner" -> "Fleet Owner"
      "broker" -> "Broker"
      else -> null
    }
    
    // Map route type to API format
    val operationalRouteTypeValue = when (routeType) {
      "local" -> "Intracity"
      "national" -> "Intercity"
      else -> null
    }
    
    compositeDisposable += loadboardRepository.updateUser(
      UpdateUserRequest(
        phoneNumber = userPrefs.phoneNumber!!,
        routePreferences = lanePreferenceList,
        truckPreferences = selectedTrucks,
        vendorType = vendorTypeValue,
        operationalRouteType = operationalRouteTypeValue
      )
    )
      .onBackground()
      .progress()
      .subscribe { _res, error ->
        if (!error) {
          val listOfLanes = ArrayList<RouteMappingModel>()
          for(item in lanePreferenceList){
            try{
            val origin = UserCity(item.origin!!.location!!,item.origin!!.locationId,item.origin!!.locationType!!)
            val destinations = StateModel(item.destination!!.location!!,item.destination!!.locationId!!,item.destination!!.locationId!!,item.destination!!.locationType!!)
            val routeMappingModel = RouteMappingModel(origin,destinations)
            listOfLanes.add(routeMappingModel)
            }catch (e:Exception){

            }
          }
          userPrefs.setLanesPreferences(listOfLanes)
          userPrefs.truckTypes = selectedTrucks.joinToString (separator = ", ")
          userUpdateLiveData.postValue(true)

        } else {
          error.handle()
          userUpdateLiveData.postValue(false)
        }
      }

  }
}