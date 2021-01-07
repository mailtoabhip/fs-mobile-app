package com.delhivery.axle.ui.selectroute.fragments.origincity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentSelectRouteOriginCityBinding
import com.delhivery.axle.ui.selectroute.fragments.OriginSelectedAction
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteBaseFragment
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.LocationUtils
import javax.inject.Inject

class SelectRouteOriginCityFragment : SelectRouteBaseFragment<FragmentSelectRouteOriginCityBinding, SelectRouteOriginCityViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: SelectRouteOriginCityFragment by lazy { SelectRouteOriginCityFragment() }
  }

  override fun getViewModelClass() = SelectRouteOriginCityViewModel::class.java

  override fun layoutId() = R.layout.fragment_select_route_origin_city

  @Inject lateinit var autoCompleteUtils: AutoCompleteUtils
  @Inject lateinit var locationUtils: LocationUtils

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    /* setup edit */
    binding.editOriginCity.setText("")

    /* observe event data */
    viewModel.eventLiveData.observe(this, EventObserver())

    /* listen for  */
    autoCompleteUtils.autoCompleteCity(binding.editOriginCity) {
      uiUtils.toggleKeyboard()
      viewModel.eventLiveData.postValue(SelectRouteOriginCitySelected(it))
    }
  }

//  private fun getLocation() {
//    val gpsAnim = binding.imgGps.fadeAnim(true, true)
//    val subscription = Single.zip(
//        locationUtils.getLocationAddress(),
//        Single.timer(2000, MILLISECONDS),
//        BiFunction<Pair<Location, Address?>, Long, Pair<Location, Address?>> { t1, _ -> t1 }
//    )
//        .onBackground()
//        .doFinally { /* location animation ends */ gpsAnim.cancel() }
//        .subscribe { locAddr, error ->
//          binding.textOriginCityName.text = if (!error) {
//            "${locAddr.second?.locality}, ${locAddr.second?.adminArea}"
//          } else {
//            "Location Error :("
//          }
//        }
//  }

  override fun onResume() {
    super.onResume()
    binding.editOriginCity.requestFocus()
    uiUtils.toggleKeyboard(false)
  }

  override fun onPause() {
    super.onPause()
    binding.editOriginCity.setText("")
    uiUtils.toggleKeyboard(true)
  }

  /**
   * Event observer
   */
  private inner class EventObserver : Observer<SelectRouteOriginCityBaseEvent> {
    override fun onChanged(t: SelectRouteOriginCityBaseEvent?) {
      t?.let { event ->
        when (event) {
          is SelectRouteOriginCityErrorEvent -> {
            uiUtils.showSnackbar(event.message)
          }
          is SelectRouteOriginCitySelected -> {
            action(OriginSelectedAction(event.originLocation))
          }
        }
      }
    }
  }

}