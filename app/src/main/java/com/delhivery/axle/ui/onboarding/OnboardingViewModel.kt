package com.delhivery.axle.ui.onboarding

import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.api.repository.NotificationRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.GlobalPrefs
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