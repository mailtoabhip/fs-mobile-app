package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.TripsResponse
import com.delhivery.orion.data.TripHistoryModel
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

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
}