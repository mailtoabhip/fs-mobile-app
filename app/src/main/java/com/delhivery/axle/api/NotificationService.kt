package com.delhivery.axle.api

import com.delhivery.axle.api.request.NotificationReadRequest
import com.delhivery.axle.api.response.BaseResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.PUT

interface NotificationService {

  /**
   * Mark notification read
   */
  @PUT("/mark_notification")
  fun markNotificationRead(
    @Body payload: NotificationReadRequest
  ): Single<BaseResponse<Any>>

}