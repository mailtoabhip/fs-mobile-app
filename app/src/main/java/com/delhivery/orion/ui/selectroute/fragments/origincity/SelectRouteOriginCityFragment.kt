package com.delhivery.orion.ui.selectroute.fragments.origincity

import android.arch.lifecycle.Observer
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.databinding.FragmentSelectRouteOriginCityBinding
import com.delhivery.orion.ui.selectroute.fragments.OriginSelectedAction
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteBaseFragment
import com.delhivery.orion.utils.AutoCompleteUtils
import com.delhivery.orion.utils.LocationFlowState.PermissionGranted
import com.delhivery.orion.utils.LocationUtils
import com.delhivery.orion.utils.extensions.fadeAnim
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class SelectRouteOriginCityFragment : SelectRouteBaseFragment<FragmentSelectRouteOriginCityBinding, SelectRouteOriginCityViewModel>(),
    SelectRouteOriginNearByLocationsDialogInterface {
  companion object {
    /* singleton instance */
    val _instance: SelectRouteOriginCityFragment by lazy { SelectRouteOriginCityFragment() }
  }

  override fun getViewModelClass() = SelectRouteOriginCityViewModel::class.java

  override fun layoutId() = R.layout.fragment_select_route_origin_city

  @Inject
  lateinit var autoCompleteUtils: AutoCompleteUtils
  @Inject
  lateinit var locationUtils: LocationUtils

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    /* setup edit */
    uiUtils.toggleKeyboard()
    binding.editOriginCity.setText("")
    binding.editOriginCity.clearFocus()

    /* observe event data */
    viewModel.eventLiveData.observe(this, EventObserver())

    /* use location as current city */
    binding.containerGpsOriginCity.setOnClickListener { updateLocationFlowState() }

    /* listen for  */
    autoCompleteUtils.autoCompleteCity(binding.editOriginCity) {
      uiUtils.toggleKeyboard()
      viewModel.fetchNearByLocations(it)
    }

    /* check and get location */
    updateLocationFlowState()
  }

  private fun getLocation() {
    val gpsAnim = binding.imgGps.fadeAnim(true, true)
    val subscription = Single.zip(
        locationUtils.getLocationAddress(),
        Single.timer(2000, MILLISECONDS),
        BiFunction<Pair<Location, Address?>, Long, Pair<Location, Address?>> { t1, _ -> t1 }
    )
        .onBackground()
        .doFinally { /* location animation ends */ gpsAnim.cancel() }
        .subscribe { locAddr, error ->
          binding.textOriginCityName.text = if (!error) {
            "${locAddr.second?.locality}, ${locAddr.second?.adminArea}"
          } else {
            "Location Error :("
          }
        }
  }

  /**
   * Update location flow
   */
  private fun updateLocationFlowState() {
    binding.textOriginCityName.text =
      if (locationUtils.getLocationPermissionFlowState() == PermissionGranted) {
        getLocation()
        "Loading..."
      } else {
        "No Location :("
      }
  }

  /**
   * Event observer
   */
  private inner class EventObserver : Observer<SelectRouteOriginCityBaseEvent> {
    override fun onChanged(t: SelectRouteOriginCityBaseEvent?) {
      t?.let { event ->
        when (event) {
          is SelectRouteOriginCityNearbyLocations -> {
            nearByLocations(event.originLocation, event.locations)
          }
          is SelectRouteOriginCityErrorEvent -> {
            uiUtils.showSnackbar(event.message)
          }
        }
      }
    }
  }

  private fun nearByLocations(
    origin: CityModel,
    locations: List<CityModel>
  ) {
    SelectRouteOriginNearByLocationsDialog(context!!, origin, locations, this).show()
  }

  override fun nearByLocationsSelected(
    selectedLocation: CityModel,
    locations: List<CityModel>
  ) {
    action(OriginSelectedAction(selectedLocation, locations))
  }
}