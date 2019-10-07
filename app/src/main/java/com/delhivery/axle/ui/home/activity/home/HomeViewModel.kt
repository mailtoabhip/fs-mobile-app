package com.delhivery.axle.ui.home.activity.home

import com.delhivery.axle.repository.NotificationRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class HomeViewModel @Inject constructor(
  private val notificationRepository: NotificationRepository,
  private val userPrefs: UserPrefs
) :
    BaseViewModel() {

  /**
   * Get/Set opened from notification flag
   */
  var fromNotification: Boolean
    get() = userPrefs.fromNotification
    set(value) {
      userPrefs.fromNotification = value
    }

  fun markNotificationRead(id: String) {
    compositeDisposable += notificationRepository.markNotificationRead(id)
        .onBackground()
        .subscribe { _, _ ->

        }
  }
}