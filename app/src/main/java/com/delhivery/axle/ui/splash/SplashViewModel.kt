package com.delhivery.axle.ui.splash

import com.delhivery.axle.repository.AuthenticationRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.splash.SplashPostState.Auth
import com.delhivery.axle.ui.splash.SplashPostState.Home
import com.delhivery.axle.ui.splash.SplashPostState.Onboarding
import com.delhivery.axle.utils.prefs.GlobalPrefs
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class SplashViewModel @Inject constructor(
  private val authenticationRepository: AuthenticationRepository,
  private val globalPrefs: GlobalPrefs,
  private val userPrefs: UserPrefs
) :
    BaseViewModel() {

  /**
   * Post splash state
   */
  fun postState() = when {
    !globalPrefs.isOnboardingCompleted -> Onboarding
    authenticationRepository.authStatus() && userPrefs.hasLoggedIn -> Home
    else -> Auth
  }
}