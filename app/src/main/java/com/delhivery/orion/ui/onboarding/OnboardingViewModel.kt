package com.delhivery.orion.ui.onboarding

import com.delhivery.orion.repository.AuthenticationRepository
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.prefs.GlobalPrefs
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