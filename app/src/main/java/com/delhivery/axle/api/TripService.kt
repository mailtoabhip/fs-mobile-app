package com.delhivery.axle.api

import com.delhivery.axle.api.request.PodRequest
import com.delhivery.axle.api.request.UpdateDispatchRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TripSummaryResponse
import com.delhivery.axle.api.response.TripsResponse
import com.delhivery.axle.data.TripHistoryModel
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Handle network calls to Trip Service
 */
interface TripService {

  /**
   * List of trips
   */
  @GET("trips/client/{userId}/")
  fun trips(
    @Path("userId") userId: String,
    @Query("limit") limit: Int,
    @Query("offset") offset: Int,
    @Query("trip_status") status: String? = null
  ): Single<BaseResponse<TripsResponse>>

  /**
   * List of trips
   */
  @GET("trips/")
  fun tripsForStatuses(
    @Query("vendor_id") userId: String,
    @Query("limit") limit: Int,
    @Query("offset") offset: Int,
    @Query("status_list") status: String? = null,
    @Query("updated_after") updatedAfter: String? = null
  ): Single<BaseResponse<TripsResponse>>

  /**
   * Trip details
   */
  @GET("trips/{transactionId}/")
  fun trip(
    @Path("transactionId") transactionId: String
  ): Single<BaseResponse<HomeTripsItemData>>

  /**
   * Trip details
   */
  @PATCH("trips/{transactionId}/")
  fun updateTrip(
    @Path("transactionId") transactionId: String,
    @Body request: PodRequest
  ): Single<BaseResponse<Any>>

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
   * Update pod dispatch details
   */
  @POST("pod_dispatch_detail")
  fun updateDispatchDetails(
    @Body request: UpdateDispatchRequest
  ): Single<BaseResponse<List<String>>>
}