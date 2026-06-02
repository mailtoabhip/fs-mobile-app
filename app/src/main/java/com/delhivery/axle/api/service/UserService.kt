package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.UpdateUserAccessRequest
import com.delhivery.axle.api.request.UpdateUserBaseCityRequest
import com.delhivery.axle.api.request.UpdateUserFCMTokenRequest
import com.delhivery.axle.api.response.*
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.UserModel
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
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

  @GET("/users/supplypartners")
  fun getOMCs(
    @Query("offset") offset: Int,
    @Query("limit") limit: Int,
    @Query("payee_type") payeeType: String
  ): Single<BaseResponse<OMCDetails>>

}