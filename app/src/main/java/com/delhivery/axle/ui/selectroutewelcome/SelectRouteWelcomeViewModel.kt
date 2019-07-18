package com.delhivery.axle.ui.selectroutewelcome

import com.delhivery.axle.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class SelectRouteWelcomeViewModel @Inject constructor(private val userRepository: UserRepository) :
    BaseViewModel() {

  /* UI binded username */
  var username: String = userRepository.username()

  fun selectRoute() {}
}