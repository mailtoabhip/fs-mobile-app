package com.delhivery.orion.ui.splash

import com.delhivery.orion.repository.AuthenticationRepository
import com.delhivery.orion.ui.base.BaseViewModel
import javax.inject.Inject

class SplashViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepository) :
    BaseViewModel() {

  /**
   * Get user auth state
   */
  fun authState() = authenticationRepository.authStatus()
}