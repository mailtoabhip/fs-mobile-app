package com.delhivery.orion.ui.selectroutewelcome

import com.delhivery.orion.repository.UserRepository
import com.delhivery.orion.ui.base.BaseViewModel
import javax.inject.Inject

class SelectRouteWelcomeViewModel @Inject constructor(private val userRepository: UserRepository) :
    BaseViewModel() {

  /* UI binded username */
  var username: String = userRepository.username()

  fun selectRoute() {}
}