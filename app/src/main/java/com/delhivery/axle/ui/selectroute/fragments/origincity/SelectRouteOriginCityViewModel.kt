package com.delhivery.axle.ui.selectroute.fragments.origincity

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.service.UserService
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class SelectRouteOriginCityViewModel @Inject constructor(private val originDataService: UserService) :
    BaseViewModel() {

  /* event live data */
  var eventLiveData = MutableLiveData<SelectRouteOriginCityBaseEvent>()

  /**
   * origin city selected, fetch and show nearyby locations
   */
//  fun fetchNearByLocations(city: CityModel) {
//    compositeDisposable += originDataService.nearByLocations(city.orion_db_city_code)
//        .convertResponse()
//        .onBackground()
//        .progress()
//        .subscribe { locations, error ->
//          if (!error) {
//            eventLiveData.postValue(SelectRouteOriginCityNearbyLocations(city, locations))
//          } else {
//            eventLiveData.postValue(SelectRouteOriginCityErrorEvent())
//          }
//        }
//  }
}