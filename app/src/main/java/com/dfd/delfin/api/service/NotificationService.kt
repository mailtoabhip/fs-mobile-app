package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.NotificationReadRequest
import com.dfd.delfin.api.response.BaseResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.PUT

/**
 * Handle network calls to Notification Service
 */
interface NotificationService {

  /**
   * Mark notification read
   */
  @PUT("/mark_notification")
  fun markNotificationRead(
    @Body payload: NotificationReadRequest
  ): Single<BaseResponse<Any>>

}