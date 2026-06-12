package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.NotificationReadRequest
import com.dfd.delfin.api.service.NotificationService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
  private val notificationService: NotificationService,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  /**
   * Mark notification with [id] as read
   */
  fun markNotificationRead(id: String) =
    notificationService.markNotificationRead(
        NotificationReadRequest.getRequest(id)
    ).convertResponse()

}