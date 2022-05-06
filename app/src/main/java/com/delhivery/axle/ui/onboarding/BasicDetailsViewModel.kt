package com.delhivery.axle.ui.onboarding

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.NotificationRepository
import com.delhivery.axle.api.request.OriginDestinations
import com.delhivery.axle.api.request.RouteDetails
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.RouteMappingModel
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.data.UserCity
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.searchcitystate.selectedCityStates
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.GlobalPrefs
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class BasicDetailsViewModel @Inject constructor(
  private val loadboardRepository: LoadboardRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var userUpdateLiveData = MutableLiveData<Boolean>()
  var selectedOrigin: CityModel? = null
  var selectedTrucks = ArrayList<String>()
  var lanePreferenceList = ArrayList<OriginDestinations>()

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
    compositeDisposable += loadboardRepository.updateUser(
      UpdateUserRequest(
        phoneNumber = userPrefs.phoneNumber!!,
        routePreferences = lanePreferenceList,
        truckPreferences = selectedTrucks

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