package com.delhivery.axle.api

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.data.CitiesResponse
import com.delhivery.axle.data.CityModel
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface CityService {

  /**
   * Search cities
   */
  @GET("/cities")
  fun searchCities(
    @Query("city_prefix") query: String
  ): Single<BaseResponse<List<CityModel>>>

  /**
   * Get all cities
   */
  @GET("/cities/list")
  fun getAllCities(
    @Query("limit") limit: Int = 3000
  ): Single<BaseResponse<CitiesResponse>>

}