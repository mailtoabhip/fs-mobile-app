package com.dfd.delfin.ui.onboarding

import com.dfd.delfin.api.repository.AuthenticationRepository
import com.dfd.delfin.api.repository.NotificationRepository
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.GlobalPrefs
import javax.inject.Inject

/**
 * View model for [OnboardingActivity]
 */
class OnboardingViewModel @Inject constructor(
  private val authenticationRepository: AuthenticationRepository,
  private val notificationRepository: NotificationRepository,
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

  fun markNotificationRead(id: String) {
    compositeDisposable += notificationRepository.markNotificationRead(id)
        .onBackground()
        .subscribe { _, _ ->

        }
  }
}