package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.RouteMappingModel
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
  /**
   * Search cities
   */
  @GET("cities/autocomplete/{query}")
  fun searchCities(
    @Path("query") query: String
  ): Single<BaseResponse<List<CityModel>>>

  /**
   * Near by cities
   */
  @GET("cities/suggestions/{cityId}")
  fun nearByLocations(
    @Path("cityId") cityId: String
  ): Single<BaseResponse<List<CityModel>>>

  /**
   * Get user route preferences
   */
  @GET("users/supplypartners/lanepreferences/{user_id}")
  fun userRoutes(
    @Path("user_id") userId: String
  ): Single<BaseResponse<List<RouteMappingModel>>>
}