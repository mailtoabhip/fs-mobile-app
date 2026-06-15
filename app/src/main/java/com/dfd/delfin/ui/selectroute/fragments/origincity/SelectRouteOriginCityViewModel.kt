package com.dfd.delfin.ui.selectroute.fragments.origincity

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.service.UserService
import com.dfd.delfin.ui.base.BaseViewModel
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