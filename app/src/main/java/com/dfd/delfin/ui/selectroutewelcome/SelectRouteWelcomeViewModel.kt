package com.dfd.delfin.ui.selectroutewelcome

import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

class SelectRouteWelcomeViewModel @Inject constructor(private val userPrefs: UserPrefs) :
    BaseViewModel() {

  /* UI binded username */
  var username: String = userPrefs.userName

}