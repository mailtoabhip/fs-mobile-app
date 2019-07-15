package com.delhivery.orion.ui.selectroute.fragments.origincity

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.api.UserService
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.convertResponse
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
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