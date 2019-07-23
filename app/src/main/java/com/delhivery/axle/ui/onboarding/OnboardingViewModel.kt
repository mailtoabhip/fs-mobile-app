package com.delhivery.axle.ui.onboarding

import com.delhivery.axle.repository.AuthenticationRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.GlobalPrefs
import javax.inject.Inject

class OnboardingViewModel @Inject constructor(
  private val authenticationRepository: AuthenticationRepository,
  private val globalPrefs: GlobalPrefs
) : BaseViewModel() {

  /**
   * Check is user is authenticated or not
   */
  fun isUserAuthenticated() = authenticationRepository.authStatus()

  /* set onboarding completed */
  fun onboardingCompleted() {
    globalPrefs.isOnboardingCompleted = true
  }
}