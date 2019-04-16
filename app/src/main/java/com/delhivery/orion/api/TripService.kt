package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.TripsResponse
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
}