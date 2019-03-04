package com.delhivery.orion.ui.selectroute

import android.arch.lifecycle.Observer
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.databinding.ActivitySelectRouteBinding
import com.delhivery.orion.ui.base.BaseLocationActivity
import com.delhivery.orion.ui.selectroute.SelectRouteUIState.OriginCity
import com.delhivery.orion.utils.LocationFlowState
import com.delhivery.orion.utils.LocationFlowState.PermissionGranted
import com.delhivery.orion.utils.extensions.fadeAnim
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS

class SelectRouteActivity : BaseLocationActivity<ActivitySelectRouteBinding, SelectRouteViewModel>() {
  override fun getViewModelClass() = SelectRouteViewModel::class.java

  override fun layoutId() = R.layout.activity_select_route

  override fun requireConnection() = true

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* observe state live data and update ui */
    viewModel.stateLiveData.observe(this, UIStateObserver())

    /* use location as current city */
    binding.containerGpsOriginCity.setOnClickListener { locationAsOrigin() }

    /* start with origin city flow */
    viewModel.state = OriginCity
  }

  private fun getLocation() {
    val gpsAnim = binding.imgGps.fadeAnim(true, true)
    compositeDisposable += Single.zip(locationUtils.getLocation()
        .flatMap { _loc ->
          val geoAddr = Geocoder(this, Locale.getDefault())
              .getFromLocation(_loc.latitude, _loc.longitude, 1)
              .firstOrNull()
          Single.just(Pair(_loc, geoAddr))
        },
        Single.timer(2000, MILLISECONDS),
        BiFunction<Pair<Location, Address?>, Long, Pair<Location, Address?>> { t1, _ -> t1 })
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
   * Use location as origin, check for permission flow as well
   */
  private fun locationAsOrigin() {
    onLocationButtonClicked()
  }

  override fun updateLocationFlowState(flowState: LocationFlowState) {
    binding.textOriginCityName.text = if (flowState == PermissionGranted) {
      getLocation()
      "Loading..."
    } else {
      "No Location :("
    }
  }

  /* UI State observer */
  inner class UIStateObserver : Observer<SelectRouteUIState> {
    override fun onChanged(_state: SelectRouteUIState?) {
      binding.state = _state
      _state?.let { state ->
        when (state) {
          OriginCity -> {
            uiUtils.toggleKeyboard()
            binding.editOriginCity.setText("")
            binding.editOriginCity.clearFocus()
            updateLocationFlowState()
          }
          else -> {

          }
        }
      }
    }
  }
}