package com.dfd.delfin.api.service

import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.data.CitiesResponse
import com.dfd.delfin.data.ClusterResponse
import com.dfd.delfin.data.SearchCitiesResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Handle network calls to City Service
 */
interface CityService {

  /**
   * Search cities
   */
  @POST("/cities/suggestion/v2/")
  fun searchCities(
    @Body request: JsonObject
  ): Single<BaseResponse<CitiesResponse>>

  /**
   * Get all cities
   */
  @POST("/cities/v2/")
  fun getAllCities(
    @Body request: JsonObject
  ): Single<BaseResponse<SearchCitiesResponse>>

  /**
   * Get Cluster id
   */
  @GET("cluster")
  fun getClusterID(
    @Query("city_id") cityId: String
  ):Single<BaseResponse<ClusterResponse>>
}