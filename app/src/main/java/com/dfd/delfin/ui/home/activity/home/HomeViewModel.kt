package com.dfd.delfin.ui.home.activity.home

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.NotificationRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
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