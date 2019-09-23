package com.delhivery.axle.ui.home

import com.delhivery.axle.repository.NotificationRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

class HomeViewModel @Inject constructor(private val notificationRepository: NotificationRepository) :
    BaseViewModel() {

  fun markNotificationRead(id: String) {
    compositeDisposable += notificationRepository.markNotificationRead(id)
        .onBackground()
        .subscribe { _, _ ->

        }
  }
}