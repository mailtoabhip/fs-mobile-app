package com.delhivery.orion.api

import com.delhivery.orion.api.request.UpdateUserBaseCityRequest
import com.delhivery.orion.api.request.UpdateUserRoutesRequest
import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.RouteMappingModel
import com.delhivery.orion.data.UserModel
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
  /**
   * Search cities
   */
  @GET("/cities")
  fun searchCities(
    @Query("city_prefix") query: String
  ): Single<BaseResponse<List<CityModel>>>

  /**
   * Near by cities
   */
  @GET("/cities/suggestions/{cityId}")
  fun nearByLocations(
    @Path("cityId") cityId: String
  ): Single<BaseResponse<List<CityModel>>>

  /**
   * Get user details
   */
  @GET("/users/supplypartners/{user_id}")
  fun userDetails(
    @Path("user_id") userId: String
  ): Single<BaseResponse<UserModel>>

  /**
   * Update user route prefs
   */
  @PATCH("/users/supplypartners/{user_id}/")
  fun updateUserRoutes(
    @Path("user_id") userId: String,
    @Body payload: UpdateUserRoutesRequest
  ): Single<Any>

  /**
   * Update base city
   */
  @PATCH("/users/supplypartners/{user_id}/")
  fun updateCity(
    @Path("user_id") userId: String,
    @Body payload: UpdateUserBaseCityRequest
  ): Single<Any>
}