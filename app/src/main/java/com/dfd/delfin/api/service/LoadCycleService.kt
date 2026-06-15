package com.dfd.delfin.api.service

import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.FrequentTripsResponse
import com.dfd.delfin.api.response.SearchTripsResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Handles network calls for load cycle API.
 * 
 * This service has been migrated from RxJava to Kotlin Coroutines.
 * All methods now use suspend functions for better integration with Flow-based architecture.
 */
interface LoadCycleService {

  /**
   * Search trips using suspend function.
   * Retrofit automatically handles coroutine execution on background thread.
   *
   * @param request JsonObject containing search parameters (origin, destination, vehicle type, etc.)
   * @return BaseResponse<SearchTripsResponse> wrapped in suspend function
   */
  @POST("/trips")
  suspend fun searchTrips(
    @Body request: JsonObject
  ): BaseResponse<SearchTripsResponse>

  /**
   * Get frequent operated lanes for vendor in last 60 days using suspend function.
   * Retrofit automatically handles coroutine execution on background thread.
   *
   * @param request JsonObject containing request parameters
   * @return BaseResponse<FrequentTripsResponse> wrapped in suspend function
   */
  @POST("/trips")
  suspend fun getFrequentLanes(
    @Body request: JsonObject
  ): BaseResponse<FrequentTripsResponse>


  /**
   * Get frequent operated lanes for vendor in last 60 days
   */
  @POST("/trips")
  fun getFrequentLanesRxJava(
    @Body request: JsonObject
  ): Single<BaseResponse<FrequentTripsResponse>>

}