package com.delhivery.axle.ui.home.activity.home

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.NotificationRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * View model for [HomeActivity]
 */
class HomeViewModel @Inject constructor(
  private val notificationRepository: NotificationRepository,
  private val userRepository: UserRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel() {

  var userUpdateLiveData = MutableLiveData<Boolean>()
  /**
   * Get/Set opened from notification flag
   */
  var fromNotification: Boolean
    get() = userPrefs.fromNotification
    set(value) {
      userPrefs.fromNotification = value
    }

  /**
   * Mark notification read
   */
  fun markNotificationRead(id: String) {
    compositeDisposable += notificationRepository.markNotificationRead(id)
        .onBackground()
        .subscribe { _, _ -> }
  }

  fun getUserDetails() {
    compositeDisposable += userRepository.getUser(false)
      .onBackground()
      .subscribe { _user, error ->
        if (!error) {
          userUpdateLiveData.postValue(true)
        } else {
          error.handle()
          userUpdateLiveData.postValue(false)
        }
      }
  }
}