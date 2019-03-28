package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.data.CityModel
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface OrionDataService {
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
}