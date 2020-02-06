package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.PodRequest
import com.delhivery.axle.api.request.UpdateDispatchRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TripSummaryResponse
import com.delhivery.axle.api.response.UploadPodResponse
import com.delhivery.axle.data.TripHistoryModel
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Handle network calls to Trip Service
 */
interface TripService {

  /**
   * Trip details
   */
  @GET("trips/{transactionId}/")
  fun trip(
    @Path("transactionId") transactionId: String
  ): Single<BaseResponse<HomeTripsItemData>>

  /**
   * Trip History
   */
  @GET("trips/history/{transactionId}/")
  fun tripHistory(
    @Path("transactionId") transactionId: String
  ): Single<BaseResponse<List<TripHistoryModel>>>

  /**
   * Get User/supplier trips summary [TripSummaryResponse]
   */
  @GET("/trips/summary/{vendor_id}/")
  fun userTripsSummary(
    @Path("vendor_id") userId: String
  ): Single<BaseResponse<TripSummaryResponse>>

  /**
   * Upload vendor POD
   */
  @POST("upload_vendor_pod")
  fun uploadPod(
    @Body request: PodRequest
  ): Single<BaseResponse<UploadPodResponse>>

  /**
   * Update pod dispatch details
   */
  @POST("pod_dispatch_detail")
  fun updateDispatchDetails(
    @Body request: UpdateDispatchRequest
  ): Single<BaseResponse<List<String>>>
}