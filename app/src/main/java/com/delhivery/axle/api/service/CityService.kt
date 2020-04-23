package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.data.CitiesResponse
import com.delhivery.axle.data.SearchCitiesResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Handle network calls to City Service
 */
interface CityService {

  /**
   * Search cities
   */
  @POST("/cities/suggestion/v1/")
  fun searchCities(
    @Body request: JsonObject
  ): Single<BaseResponse<CitiesResponse>>

  /**
   * Get all cities
   */
  @POST("/cities/v1/")
  fun getAllCities(
    @Body request: JsonObject
  ): Single<BaseResponse<SearchCitiesResponse>>

}