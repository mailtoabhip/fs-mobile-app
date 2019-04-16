package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.TripsResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TripService {

  /**
   * List of trips
   */
  @GET("trips/")
  fun trips(
    @Query("limit") limit: Int,
    @Query("offset") offset: Int
  ): Single<BaseResponse<TripsResponse>>
}