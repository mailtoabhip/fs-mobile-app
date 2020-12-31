package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.UpdateUserAccessRequest
import com.delhivery.axle.api.request.UpdateUserBaseCityRequest
import com.delhivery.axle.api.request.UpdateUserFCMTokenRequest
import com.delhivery.axle.api.response.BaseMessageResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.UserModel
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Handle network calls to User Service
 */
interface UserService {

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
    @Body payload: JsonObject
  ): Single<Any>

  /**
   * Update base city
   */
  @PATCH("/users/supplypartners/{user_id}/")
  fun updateCity(
    @Path("user_id") userId: String,
    @Body payload: UpdateUserBaseCityRequest
  ): Single<Any>

  /**
   * Update user app access
   */
  @PATCH("/users/supplypartners/{user_id}/")
  fun updateUserAppAccess(
    @Path("user_id") userId: String,
    @Body payload: UpdateUserAccessRequest
  ): Single<Any>

  /**
   * Update FCM token
   */
  @PATCH("/users/supplypartners/{user_id}")
  fun updateFCMToken(
    @Path("user_id") userId: String,
    @Body payload: UpdateUserFCMTokenRequest
  ): Single<BaseMessageResponse>

  /**
   * Get team member's detail
   */
  @GET("/users/supplypartners")
  fun getTeamMembers(
    @Query("offset") offset: Int,
    @Query("limit") limit: Int,
    @Query("inc_all_users") includeAllUsers: Boolean,
    @Query("sp_id") sp_id: String
  ): Single<BaseResponse<List<UserModel>>>

}