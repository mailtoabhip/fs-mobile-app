package com.delhivery.orion.ui.splash

import com.delhivery.orion.repository.AuthenticationRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.splash.SplashPostState.Auth
import com.delhivery.orion.ui.splash.SplashPostState.Home
import com.delhivery.orion.ui.splash.SplashPostState.Onboarding
import com.delhivery.orion.utils.prefs.GlobalPrefs
import javax.inject.Inject

class SplashViewModel @Inject constructor(
  private val authenticationRepository: AuthenticationRepository,
  private val globalPrefs: GlobalPrefs
) :
    BaseViewModel() {

  /**
   * Post splash state
   */
  fun postState() = when {
    !globalPrefs.isOnboardingCompleted -> Onboarding
    authenticationRepository.authStatus() -> Home
    else -> Auth
  }
}