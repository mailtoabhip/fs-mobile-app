package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.AcceptTransactionBidRequest
import com.dfd.delfin.api.request.PodRequest
import com.dfd.delfin.api.request.UpdateDispatchRequest
import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.TripPaymentResponse
import com.dfd.delfin.api.response.TripSummaryResponse
import com.dfd.delfin.data.TripHistoryModel
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
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
   * Trip details
   */
  @PATCH("trips/actions/{transactionId}/")
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

  /**
   * Get bulk payment summary of trips
   */
  @POST("/trips/bulk_payment_summary/")
  fun fetchTripsPayments(
    @Body request: JsonObject
  ): Single<BaseResponse<List<TripPaymentResponse>>>


  @POST("/create_and_accept_adhoc_bid")
  fun acceptTripBid(
    @Body request: AcceptTransactionBidRequest
  ): Single<BaseResponse<Any>>
}