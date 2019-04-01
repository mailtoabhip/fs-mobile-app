package com.delhivery.orion.ui.selectroute

import com.delhivery.orion.api.UserService
import com.delhivery.orion.data.RouteModel
import com.delhivery.orion.ui.base.BaseViewModel
import javax.inject.Inject

class SelectRouteViewModel @Inject constructor(
  private val userService: UserService
) : BaseViewModel() {

  /* selected route models */
  var routes = mutableListOf<RouteModel>()

}