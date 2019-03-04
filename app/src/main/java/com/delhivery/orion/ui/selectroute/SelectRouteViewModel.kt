package com.delhivery.orion.ui.selectroute

import android.arch.lifecycle.MutableLiveData
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.selectroute.SelectRouteUIState.OriginCity
import javax.inject.Inject

class SelectRouteViewModel @Inject constructor() : BaseViewModel() {

  /* states */
  var stateLiveData = MutableLiveData<SelectRouteUIState>()
  var state: SelectRouteUIState = OriginCity
    set(value) {
      stateLiveData.postValue(value)
    }
}