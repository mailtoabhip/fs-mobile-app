package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.FrequentTripsResponse
import com.delhivery.axle.api.response.SearchTripsResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Handles network calls for city api
 */
interface LoadCycleService {

  /**
   * Search trips
   */
  @POST("/trips")
  fun searchTrips(
    @Body request: JsonObject
  ): Single<BaseResponse<SearchTripsResponse>>

  /**
   * Get frequent operated lanes for vendor in last 60 days
   */
  @POST("/trips")
  fun getFrequentLanes(
          @Body request: JsonObject
  ): Single<BaseResponse<FrequentTripsResponse>>

}