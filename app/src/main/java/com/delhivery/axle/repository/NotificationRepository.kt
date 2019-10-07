package com.delhivery.axle.repository

import com.delhivery.axle.api.NotificationService
import com.delhivery.axle.api.request.NotificationReadRequest
import com.delhivery.axle.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
  private val notificationService: NotificationService
) : BaseRepository() {

  /**
   * Mark notification with [id] as read
   */
  fun markNotificationRead(id: String) =
    notificationService.markNotificationRead(
        NotificationReadRequest.getRequest(id)
    ).convertResponse()

}