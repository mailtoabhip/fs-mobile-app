package com.delhivery.axle.ui.selectroute.fragments.origincity

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.UserService
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class SelectRouteOriginCityViewModel @Inject constructor(private val originDataService: UserService) :
    BaseViewModel() {

  /* event live data */
  var eventLiveData = MutableLiveData<SelectRouteOriginCityBaseEvent>()

  /**
   * origin city selected, fetch and show nearyby locations
   */
  fun fetchNearByLocations(city: CityModel) {
    compositeDisposable += originDataService.nearByLocations(city.cityId)
        .convertResponse()
        .onBackground()
        .progress()
        .subscribe { locations, error ->
          if (!error) {
            eventLiveData.postValue(SelectRouteOriginCityNearbyLocations(city, locations))
          } else {
            eventLiveData.postValue(SelectRouteOriginCityErrorEvent())
          }
        }
  }
}