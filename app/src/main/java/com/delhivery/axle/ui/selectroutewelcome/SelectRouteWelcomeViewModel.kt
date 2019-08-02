package com.delhivery.axle.ui.selectroutewelcome

import com.delhivery.axle.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class SelectRouteWelcomeViewModel @Inject constructor(private val userPrefs: UserPrefs) :
    BaseViewModel() {

  /* UI binded username */
  var username: String = userPrefs.userName

}